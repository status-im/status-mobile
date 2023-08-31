(ns quo2.components.text-combinations.style)

(defn container
  [container-style onboarding?]
  (merge
   {:flex              (when-not onboarding? 1)
    :margin-horizontal 20}
   container-style))

(defn title-container [onboarding?]
  {:flex           (when-not onboarding? 1)
   :flex-direction :row
   :align-items    :center})

(def avatar-container
  {:margin-right 9})

(def description-description-text
  {:margin-top 8})
