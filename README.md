# clj-boolean

A Clojure library for operating on free Boolean algebras.

## Usage

This library operates on a flexible Boolean algebra AST with user-defined leaf nodes representing the generators. It's convenient to define your generators or generator _families_ up-front.

```clojure
(ns user
  (:require [clj-boolean.syntax :as syn]))

;; Define a generator representing the proposition that the document is
;; a blog post, represented as a query node with the tag ::blog-post.
(def blog-post (syn/node ::blog-post))

;; Define a family of generators representing propositions that the document
;; contains a certain phrase. This generator family is represented by a function
;; producing query nodes with the tag ::phrase and a property called :text.
(defn phrase
  [text]
  (syn/node ::phrase :text text))

(def expression
  (syn/and
	blog-post
    (phrase "clojure")
    (phrase "datomic")))
```

## License

Copyright © 2018 Tom Savage

MIT License