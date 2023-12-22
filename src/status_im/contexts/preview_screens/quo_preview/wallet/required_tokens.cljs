(ns status-im.contexts.preview-screens.quo-preview.wallet.required-tokens
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :token}
              {:key :collectible}]}
   {:key  :divider?
    :type :boolean}])

(def token-descriptor
  [{:key  :amount
    :type :text}
   {:key     :token
    :type    :select
    :options [{:key "SNT"}
              {:key "ETH"}]}])

(def collectible-descriptor
  [{:key  :collectible-name
    :type :text}])

(defn view
  []
  (let
    [state
     (reagent/atom
      {:type                :token
       :collectible-img-src (resources/mock-images :collectible)
       :collectible-name    "Collectible name"
       :token               "SNT"
       :amount              "100"
       :divider?            false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor (concat descriptor
                            (case (:type @state)
                              :token       token-descriptor
                              :collectible collectible-descriptor
                              nil))}
       [quo/required-tokens @state]])))
