(ns status-im2.contexts.emoji-picker.data
  (:require [status-im2.contexts.emoji-picker.constants :as constants]
            [utils.transforms :as transforms]))

;; The dataset is grouped by number to reduce the file size.
;; It is then categorized below:
;; - people (0 and 1)
;; - nature (3)
;; - food (4)
;; - activity (6)
;; - travel (5)
;; - objects (7)
;; - symbols (8)
;; - flags (9)
;;
;; NOTE: smileys & emoticons (0) and people (1) are grouped in one section

(def emoji-data (transforms/js->clj (js/require "../src/js/data/emojis.json")))

(def list-data
  [{:title :t/emoji-people
    :icon  :i/faces ;; 0 and 1
    :id    :people
    :data  []}
   {:title :t/emoji-nature
    :icon  :i/nature ;; 3
    :id    :nature
    :data  []}
   {:title :t/emoji-food
    :icon  :i/food ;; 4
    :id    :food
    :data  []}
   {:title :t/emoji-activity
    :icon  :i/activity ;; 6
    :id    :activity
    :data  []}
   {:title :t/emoji-travel
    :icon  :i/travel ;; 5
    :id    :travel
    :data  []}
   {:title :t/emoji-objects
    :icon  :i/objects ;; 7
    :id    :objects
    :data  []}
   {:title :t/emoji-symbols
    :icon  :i/symbols ;; 8
    :id    :symbols
    :data  []}
   {:title :t/emoji-flags
    :icon  :i/flags ;; 9
    :id    :flags
    :data  []}])

(def categorized-and-partitioned
  (->> (reduce
        (fn [acc {:keys [group] :as emoji}]
          (case group

            0
            (update-in acc [0 :data] conj emoji)

            1
            (update-in acc [0 :data] conj emoji)

            3
            (update-in acc [1 :data] conj emoji)

            4
            (update-in acc [2 :data] conj emoji)

            5
            (update-in acc [4 :data] conj emoji)

            6
            (update-in acc [3 :data] conj emoji)

            7
            (update-in acc [5 :data] conj emoji)

            8
            (update-in acc [6 :data] conj emoji)

            9
            (update-in acc [7 :data] conj emoji)))
        list-data
        emoji-data)
       (reduce (fn [acc {:keys [data] :as item}]
                 (conj acc (assoc item :data (partition-all constants/emojis-per-row data))))
               [])))

(def flatten-data
  (mapcat (fn [{:keys [title id data]}]
            (into [{:title title :id id :header? true}] data))
   categorized-and-partitioned))

(def filter-section-header-index
  (keep-indexed #(when (:header? %2) %1) flatten-data))

(defn get-section-header-index-in-data
  [index]
  (nth filter-section-header-index index))

(def section-group
  {0 :people
   1 :people
   3 :nature
   4 :food
   6 :activity
   5 :travel
   7 :objects
   8 :symbols
   9 :flags})
