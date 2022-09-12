(ns quo2.screens.list-items.preview-lists
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.list-items.preview-list :as quo2]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}
                            {:key   16
                             :value "16"}]}
                 {:label "List Size"
                  :key   :list-size
                  :default 10
                  :type  :text}])

(def user-list-mock
  [{:full-name "ABC DEF"}
   {:full-name "GHI JKL"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(defn cool-preview []
  (let [state (reagent/atom {:type      :user
                             :size      32
                             :list-size 10})
        type  (reagent/cursor state [:type])]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/preview-list @state
         ;; Mocked list items
         (case @type
           :user user-list-mock)]]])))

(defn preview-preview-lists []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
