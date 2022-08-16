(ns quo2.components.context-tags
  (:require [quo2.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [re-frame.core :as re-frame]
            [quo2.components.text :as text]
            [quo2.components.group-avatar :as group-avatar]
            [quo.react-native :as rn]))

(defn padding-left-for-type [type]
  (case type
    :group-avatar 3
    8))

(defn trim-public-key [pk]
  (str (subs pk 0 6) "..." (subs pk (- (count pk) 3))))

(defn base-tag [_ _]
  (fn [{:keys [override-theme style]} & children]
    (let [theme (or override-theme (quo.theme/get-theme))]
      (into
       [rn/view
        (merge
         {:border-radius 100
          :padding-vertical 3
          :flex-direction :row
          :padding-right 8
          :padding-left 8
          :background-color (if (= theme :light)
                              colors/neutral-10
                              colors/neutral-80)} style)]
       children))))

(defn group-avatar-tag [_ _]
  (fn [label opts]
    [base-tag {:style {:padding-left 3}}
     [group-avatar/group-avatar opts]
     [text/text {:weight :medium
                 :size :paragraph-2}

      (str " " label)]]))

(defn public-key-tag [_ _]
  (fn [params public-key]
    [base-tag params
     [text/text {:weight :monospace
                 :size :paragraph-2}
      (trim-public-key public-key)]]))

(defn user-avatar-tag [_ _]
  (fn [params public-key]
    (let [{:keys [name photo]} @(re-frame/subscribe [:contacts/name-and-photo public-key])]
      [base-tag (assoc-in params [:style :padding-left] 3)
       [rn/image {:style {:width 20
                          :border-radius 10
                          :background-color :white
                          :height 20}
                  :source {:uri photo}}]
       [text/text {:weight :medium
                   :size :paragraph-2}

        (str " " name)]])))
