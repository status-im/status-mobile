(ns status-im.contexts.preview.quo.wallet.account-origin
  (:require
    [quo.components.wallet.account-origin.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:type            :default-keypair
                             :stored          :on-keycard
                             :profile-picture (resources/get-mock-image :user-picture-male5)
                             :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                             :user-name       "Alisher Yakupov"
                             :on-press        #(js/alert "pressed")})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/account-origin @state]])))
