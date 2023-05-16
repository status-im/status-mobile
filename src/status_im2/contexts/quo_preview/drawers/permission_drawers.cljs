(ns status-im2.contexts.quo-preview.drawers.permission-drawers
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.common.resources :as resources]))

(defn example-1
  []
  [:<>
   [quo/text {:style {:margin-right 4}} "Hold"]
   [quo/permission-tag
    {:size             24
     :locked           false
     :tokens           [{:id    1
                         :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 5 :token-icon (resources/get-mock-image :status-logo)}]}]
     :background-color (colors/theme-colors
                        colors/neutral-10
                        colors/neutral-80)}]
   [quo/text
    {:style {:margin-left  4
             :margin-right 4}} "Or"]
   [quo/permission-tag
    {:size             24
     :locked           false
     :tokens           [{:id    1
                         :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 2 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 3 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 4 :token-icon (resources/get-mock-image :status-logo)}
                                 {:id 5 :token-icon (resources/get-mock-image :status-logo)}]}]
     :background-color (colors/theme-colors
                        colors/neutral-10
                        colors/neutral-80)}]
   [quo/text {:style {:margin-left 4}} "To post"]])

(defn example-2
  []
  [:<>
   [quo/text {:style {:margin-right 4}} "Hold"]
   [quo/token-tag
    {:size          :small
     :token-img-src (resources/get-token :eth)} "ETH"]
   [quo/text {:style {:margin-left 4}} "To post"]])

(defn example-3
  []
  [:<>
   [quo/icon :i/communities {:color (colors/theme-colors colors/neutral-100 colors/white)}]
   [quo/text {:style {:margin-right 4}} "Join community to post"]])

(defn cool-preview
  []
  (fn []
    [:<>
     [rn/view {:margin-top 60}
      [quo/permission-context [example-1] #(js/alert "drawer pressed")]]
     [rn/view {:margin-top 60}
      [quo/permission-context [example-2] #(js/alert "drawer pressed")]]
     [rn/view {:margin-top 60}
      [quo/permission-context [example-3] #(js/alert "drawer pressed")]]]))

(defn preview-permission-drawers
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/neutral-20 colors/neutral-100)}
    :flex  1}
   [rn/flat-list
    {:flex                         1
     :nested-scroll-enabled        true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       hash}]])
