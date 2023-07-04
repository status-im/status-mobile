(ns status-im2.contexts.quo-preview.tags.permission-tag
  (:require [quo2.components.tags.permission-tag :as permission-tag]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:label "Locked:"
    :key   :locked?
    :type  :boolean}])

(def community-tokens
  [{:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id 2 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id    1
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id 2 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id 3 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    3
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id 2 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id 3 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]}
   {:tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    2
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}]}
             {:id    3
              :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 5 :token-icon (resources/get-mock-image :status-logo)}
                      {:id 6 :token-icon (resources/get-mock-image :status-logo)}]}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:size 32})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :align-self       :center
          :justify-content  :center}
         (when @state
           (for [{:keys [tokens]} community-tokens]
             ^{:key tokens}
             [rn/view
              {:margin-top 20
               :align-self :flex-end}
              [permission-tag/tag
               (merge @state
                      {:tokens           tokens
                       :background-color (colors/theme-colors
                                          colors/neutral-10
                                          colors/neutral-80)})]]))]]])))

(defn preview-permission-tag
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
