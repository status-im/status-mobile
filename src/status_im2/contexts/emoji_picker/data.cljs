(ns status-im2.contexts.emoji-picker.data
  (:require [status-im2.contexts.emoji-picker.constants :as constants]
            [utils.transforms :as transforms]))

;; The dataset constains `group` key which holds a number to reduce the file size.
;; It is then categorized below:
;; - smileys-emotion (0)
;; - people-body (1)
;; - animals-nature (3)
;; - food-drink (4)
;; - activity (6)
;; - travel-places (5)
;; - objects (7)
;; - symbols (8)
;; - flags (9)
;;
;; NOTE:
;; - smileys & emoticons (0) and people (1) are grouped in one section in the app
;; - The emoji components (https://symbl.cc/en/emoji/component) which are group 2 are not used
;;   in the app and are removed from the dataset.

(def emoji-data (transforms/js->clj (js/require "../resources/data/emojis/en.json")))

(def categories
  [{:title :t/emoji-people ;; 0 and 1
    :icon  :i/faces
    :id    :people
    :data  []}
   {:title :t/emoji-nature ;; 3
    :icon  :i/nature
    :id    :nature
    :data  []}
   {:title :t/emoji-food ;; 4
    :icon  :i/food
    :id    :food
    :data  []}
   {:title :t/emoji-activity ;; 6
    :icon  :i/activity
    :id    :activity
    :data  []}
   {:title :t/emoji-travel ;; 5
    :icon  :i/travel
    :id    :travel
    :data  []}
   {:title :t/emoji-objects ;; 7
    :icon  :i/objects
    :id    :objects
    :data  []}
   {:title :t/emoji-symbols ;; 8
    :icon  :i/symbols
    :id    :symbols
    :data  []}
   {:title :t/emoji-flags ;; 9
    :icon  :i/flags
    :id    :flags
    :data  []}])

(defn emoji-group->category
  [group]
  (case group
    (0 1) {:index 0 :id :people}
    3     {:index 1 :id :nature}
    4     {:index 2 :id :food}
    5     {:index 4 :id :travel}
    6     {:index 3 :id :activity}
    7     {:index 5 :id :objects}
    8     {:index 6 :id :symbols}
    9     {:index 7 :id :flags}
    nil))

(def ^:private categorized-and-partitioned
  (->> emoji-data
       (reduce (fn [acc {:keys [group] :as emoji}]
                 (update-in acc [(-> (emoji-group->category group) :index) :data] conj emoji))
               categories)
       (reduce (fn [acc {:keys [data] :as item}]
                 (conj acc (assoc item :data (partition-all constants/emojis-per-row data))))
               [])))

(def flatten-data
  (mapcat (fn [{:keys [title id data]}]
            (into [{:title title :id id :header? true}] data))
   categorized-and-partitioned))

(def ^:private filter-section-header-index
  (keep-indexed #(when (:header? %2) %1) flatten-data))

(defn get-section-header-index-in-data
  [index]
  (nth filter-section-header-index index))
