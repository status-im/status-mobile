(ns status-im.contexts.preview.quo.cards.wallet-card
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key  :subtitle
    :type :text}
   {:key  :dismissible?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:image          (resources/get-image :buy)
                             :title          "Buy"
                             :subtitle       "Start investing now"
                             :on-press       #(js/alert "Item pressed")
                             :on-press-close #(js/alert "Close pressed")
                             :dismissible?   false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [quo/wallet-card @state]])))
