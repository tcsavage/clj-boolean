# Optimisation pass: distrib

This pass leverages the distributive properties of logical conjunction and disjunction to eliminate repeated occurrences of large subqueries. To wit:

	((P ∧ Q) ∨ (P ∧ R)) ⇔ (P ∧ (Q ∨ R))
	and
	((P ∨ Q) ∧ (P ∨ R)) ⇔ (P ∨ (Q ∧ R))

In the above equivalences, `P` is a repeated term which can be "factored out" so we only need to include it once. When `P` is a large subquery, this can potentially improve performance.

## Terminology

The implementation makes use of terminology which I don't believe to be standard. In the following equivalence:

	((P ∧ Q) ∨ (P ∧ R)) ⇔ (P ∧ (Q ∨ R))

We say conjunction (∧) distributes over disjunction (∨). Therefore I am calling conjunction the "distributor", and disjunction is the "distributee".

## Implementation

Due to the variadic definition of conjunction and disjunction (i.e. they are not binary operators) the implementation needs to be a little bit more complicated. For example, an `and` node may contain more than just two `or` nodes, but also any other node as well. This complication is fairly straight-forward to handle: simply extract the `or` nodes, wrap them in their own `and`, optimise that and re-insert.

### Weighting
Depending on the query we're analysing, there may be many common factors we could theoretically extract, so which do we pick?