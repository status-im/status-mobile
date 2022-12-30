(ns status-im2.contexts.quo-preview.list-items.preview-lists
  (:require [quo2.components.list-items.preview-list :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}
              {:key   16
               :value "16"}]}
   {:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :user
               :value "User"}
              {:key   :photo
               :value "Photo"}]}
   {:label   "List Size"
    :key     :list-size
    :default 10
    :type    :text}])

;; Mocked list items
(def user-list
  [{:full-name "ABC DEF"}
   {:full-name "GHI JKL"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(def photos-list
  [{:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo4)}
   {:source (resources/get-mock-image :photo5)}
   {:source (resources/get-mock-image :photo6)}])

(defn cool-preview
  []
  (let [state (reagent/atom {:type               :user
                             :size               32
                             :list-size          10
                             :more-than-99-label (i18n/label :counter-99-plus)})
        type  (reagent/cursor state [:type])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/preview-list @state
          (case @type
            :user  user-list
            :photo photos-list)]]]])))

(defn preview-preview-lists
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
