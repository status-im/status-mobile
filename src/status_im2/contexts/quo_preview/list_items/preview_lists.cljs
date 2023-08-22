(ns status-im2.contexts.quo-preview.list-items.preview-lists
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as quo.resources]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}
              {:key   16
               :value "16"}]}
   {:key     :type
    :type    :select
    :options [{:key :user}
              {:key :photo}
              {:key :network}]}
   {:key     :list-size
    :default 10
    :type    :text}])

(def user-list
  [{:full-name "ABC DEF"}
   {:full-name "GHI JKL"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(def photos-list
  [{:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}])

(def networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn view
  []
  (let [state (reagent/atom {:type               :user
                             :size               32
                             :list-size          10
                             :more-than-99-label "99+"})
        type  (reagent/cursor state [:type])]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/preview-list @state
        (case @type
          :user    user-list
          :photo   photos-list
          :network networks-list)]])))
