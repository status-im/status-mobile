(ns status-im.contexts.preview-screens.quo-preview.tags.permission-tag
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:key  :locked?
    :type :boolean}])

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

(defn view
  []
  (let [state (reagent/atom {:size 32})]
    (fn []
      [preview/preview-container
       {:state                     state
        :component-container-style {:margin-bottom 40}
        :descriptor                descriptor}
       (when @state
         (for [{:keys [tokens]} community-tokens]
           ^{:key tokens}
           [rn/view
            {:margin-top 20
             :align-self :flex-end}
            [quo/permission-tag
             (merge @state
                    {:tokens           tokens
                     :background-color (colors/theme-colors
                                        colors/neutral-10
                                        colors/neutral-80)})]]))])))
