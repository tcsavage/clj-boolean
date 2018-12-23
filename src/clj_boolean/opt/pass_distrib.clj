(ns clj-boolean.opt.pass-distrib
  "Take advantage of the distributive property to eliminate duplicated sub-queries.
  See /doc/pass_distrib.md for details."
  (:require [clj-boolean.analysis :as analysis]
            [clj-boolean.control :as control]
            [clj-boolean.syntax :as syn]
            [clj-boolean.timing :as timing]
            [clojure.math.combinatorics :as combo]
            [clojure.set :as set]
            [clojure.tools.logging :as log]))

(defmulti ^{:pass-name "distrib"} pass syn/node-tag)

(defmethod pass :default [node] node)

(def distributor<->distributee
  ;; AND (conjunction) distributes over OR (disjunction) and vice-sersa.
  {:clj-boolean.syntax/and :clj-boolean.syntax/or
   :clj-boolean.syntax/or :clj-boolean.syntax/and})

(def tag->ctor
  ;; This helps us construct new nodes from the tag name.
  {:clj-boolean.syntax/and syn/and
   :clj-boolean.syntax/or syn/or})

(defn keep-supersets
  "Takes a collection of sets and removes any which are wholely contained in any others."
  [sets]
  (loop [pool (set sets) set-list sets]
    (if-let [[s & ss] (seq set-list)]
      (recur (set/difference pool (set/select #(and (not= s %) (set/superset? s %)) pool)) ss)
      pool)))

(defn shared-term-sets
  "Returns a mapping of sets of common terms to the set of sets containing them."
  [term-sets]
  (log/debugf "Looking for shared terms from %d sets" (count term-sets))
  (let [term-frequencies (apply merge-with + (mapv frequencies term-sets))
        duplicated-terms (mapv first (filter #(> (second %) 1) term-frequencies))
        dupli-groups (mapv set (filter #(> (count %) 0) (combo/subsets duplicated-terms)))
        mapping (apply merge-with set/union
                       (for [group dupli-groups
                             :let [supersets (filter (partial set/subset? group) term-sets)]
                             :when (> (count supersets) 1)]
                         {(set supersets) #{group}}))]
    (into {} (mapcat (fn [[ss ts]] (mapv #(vector % ss) (keep-supersets ts))) mapping))))

(defn weight-reduction
  "Calculates the reduction of the total weight of groups after shared terms are extracted.
  Let w bet the weight of the shared terms and G be the set of groups containing the terms.
  The weight reduction by extracting these common terms is w * (|G| - 1)."
  [shared groups]
  (let [weight-shared (apply + (mapv :weight shared))]
    (* weight-shared (dec (count groups)))))

(defn select-best-factors
  "Calculates the most heavily-weighted subset set of terms in term-sets and returns it with the
  set of sets which contain them."
  [weight-fn term-sets]
  (letfn [(tag-with-weight [term] {:term term :weight (weight-fn term)})]
    (let [weighted-term-sets (mapv (comp set (partial mapv tag-with-weight)) term-sets)
          sharing-info (shared-term-sets weighted-term-sets)]
      (log/debugf "Found %d shared terms sets" (count sharing-info))
      (when (seq sharing-info)
        (let [[best-common best-group] (apply max-key (partial apply weight-reduction) sharing-info)]
          {:common (set (mapv :term best-common))
           :sets (set (mapv (comp set (partial mapv :term)) best-group))})))))

(defn distrib*
  "Takes a collection of distributor nodes"
  [distributor-nodes distributor-tag distributee-tag tracker]
  (control/cycle
   tracker [distributor-nodes distributor-tag distributee-tag]
   (let [distributor-ctor (tag->ctor distributor-tag)
         distributee-ctor (tag->ctor distributee-tag)
         ;; Make a set of sets of the children of distributor nodes.
         distributor-term-sets (set (mapv (comp set :children) distributor-nodes))]
     ;; Look for some common terms we can extract. If we can't find any, we're just going to
     ;; re-assemble the node.
     (log/debug "Looking for best factors")
     (if-let [{common :common sources :sets} (select-best-factors analysis/query-weight distributor-term-sets)]
       ;; Collect all the term sets we aren't touching and remove common terms from those that we are.
       (let [remaining-term-sets (set/difference distributor-term-sets sources)
             sources-without-common (mapv #(set/difference % common) sources)]
         ;; If we have some term sets we're not touching, we need to make sure the common terms
         ;; we're going to pull out don't affect them. So we wrap them up in their own little
         ;; bubble and then see if we can factor some common terms out of them.
         (if (seq remaining-term-sets)
           (apply vector
                  (apply distributor-ctor
                         (apply distributee-ctor (mapv (partial apply distributor-ctor) sources-without-common))
                         common)
                  (distrib* (mapv (partial apply distributor-ctor) remaining-term-sets) distributor-tag distributee-tag tracker))
           (apply distributor-ctor
                  (apply distributee-ctor (mapv (partial apply distributor-ctor) sources-without-common))
                  common)))
       ;; Re-assemble.
       distributor-nodes))))

(defn with-distributors
  [distributee f & args]
  (let [distributee-tag (syn/node-tag distributee)
        distributor-tag (distributor<->distributee distributee-tag)]
    (apply
     syn/with-select-children
     distributee
     #(= distributor-tag (syn/node-tag %))
     f
     (list* distributor-tag distributee-tag args))))

(defn distrib
  [node tracker]
  (control/cycle
   tracker node
   (timing/time-limit
    (with-distributors node distrib* tracker)
    100000
    (log/error "distrib pass timed-out"))))

(defmethod pass :clj-boolean.syntax/and [node] (distrib node (control/cycle-detector)))
(defmethod pass :clj-boolean.syntax/or [node] (distrib node (control/cycle-detector)))
