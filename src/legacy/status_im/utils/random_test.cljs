(ns legacy.status-im.utils.random-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.utils.random :as random]))

(deftest test-seeded-rand-int
  ;try with one seed
  (let [seed 0
        gen  (random/rand-gen seed)]
    (is (= (random/seeded-rand-int gen 100) 54))
    (is (= (random/seeded-rand-int gen 100) 59))
    (is (= (random/seeded-rand-int gen 10) 7))
    (is (= (random/seeded-rand-int gen 10) 8)))
  ;repeat with the same seed, but generator re-set
  (let [seed 0
        gen  (random/rand-gen seed)]
    (is (= (random/seeded-rand-int gen 100) 54))
    (is (= (random/seeded-rand-int gen 100) 59))
    (is (= (random/seeded-rand-int gen 10) 7))
    (is (= (random/seeded-rand-int gen 10) 8)))
  ;a string seed
  (let [seed "6ec565f4fec866a54761524f603cf037e20c2bfa"
        gen  (random/rand-gen seed)]
    (is (= (random/seeded-rand-int gen 100) 7))
    (is (= (random/seeded-rand-int gen 100) 4))
    (is (= (random/seeded-rand-int gen 10) 5))
    (is (= (random/seeded-rand-int gen 10) 7)))
  ;a string seed again
  (let [seed "6ec565f4fec866a54761524f603cf037e20c2bfa"
        gen  (random/rand-gen seed)]
    (is (= (random/seeded-rand-int gen 100) 7))
    (is (= (random/seeded-rand-int gen 100) 4))
    (is (= (random/seeded-rand-int gen 10) 5))
    (is (= (random/seeded-rand-int gen 10) 7)))
  ;nil seed is the same as 0
  (let [seed 0
        gen  (random/rand-gen seed)]
    (is (= (random/seeded-rand-int gen 100) 54))
    (is (= (random/seeded-rand-int gen 100) 59))
    (is (= (random/seeded-rand-int gen 10) 7))
    (is (= (random/seeded-rand-int gen 10) 8))))

(deftest test-seeded-rand-int-boundaries
  (let [seed   "6ec565f4fec866a54761524f603cf037e20c2bfa"
        n      10
        gen    (random/rand-gen seed)
        sample (into #{} (repeatedly 1000 #(random/seeded-rand-int gen n)))]
    ;no result should be negative
    (is (empty? (filter #(< % 0) sample)))
    ;no result should be larger than n
    (is (empty? (filter #(>= % n) sample)))
    ;and while there is a very small probability
    ;it is very unlikely and probably wrong if all 1000 calls
    ;got us the same number.
    (is (> (count sample) 1))))

(deftest test-seeded-rand-nth
  (let [seed "6ec565f4fec866a54761524f603cf037e20c2bfa"
        gen  (random/rand-gen seed)
        coll [:a :b :c :d :e :f :g]]
    (is (= (random/seeded-rand-nth gen coll) :a))
    (is (= (random/seeded-rand-nth gen coll) :a))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :f))
    (is (= (random/seeded-rand-nth gen coll) :c)))
  ;try again with the same seed but gen reset
  (let [seed "6ec565f4fec866a54761524f603cf037e20c2bfa"
        gen  (random/rand-gen seed)
        coll [:a :b :c :d :e :f :g]]
    (is (= (random/seeded-rand-nth gen coll) :a))
    (is (= (random/seeded-rand-nth gen coll) :a))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :f))
    (is (= (random/seeded-rand-nth gen coll) :c)))
  ;try again with a different seed
  (let [seed "57348975ff9199ca636207a396b915d6b6a675b4"
        gen  (random/rand-gen seed)
        coll [:a :b :c :d :e :f :g]]
    (is (= (random/seeded-rand-nth gen coll) :f))
    (is (= (random/seeded-rand-nth gen coll) :c))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :a)))
  ;and re-set
  (let [seed "57348975ff9199ca636207a396b915d6b6a675b4"
        gen  (random/rand-gen seed)
        coll [:a :b :c :d :e :f :g]]
    (is (= (random/seeded-rand-nth gen coll) :f))
    (is (= (random/seeded-rand-nth gen coll) :c))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :e))
    (is (= (random/seeded-rand-nth gen coll) :a))))
