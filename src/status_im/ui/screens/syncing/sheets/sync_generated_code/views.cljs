(ns status-im.ui.screens.syncing.sheets.sync-generated-code.views
  (:require [clojure.string :as string]
            [quo.react-native :as rn]
            [status-im.ui.screens.syncing.sheets.sync-generated-code.styles :as styles]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [quo2.components.buttons.button :as quo2]
            [quo2.components.info.information-box :as information-box]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.native-module.core :as status]
            [status-im.react-native.resources :as resources]
            [taoensso.timbre :as log]))

(def example-key "2:4FHRnp:Q4:uqnnMwVUfJc2Fkcaojet8F1ufKC3hZdGEt47joyBx9yd:BbnZ7Gc66t54a9kEFCf7FW8SGQuYypwHVeNkRYeNoqV6:3")

(def configJson {:keystorePath "lalalal" ;; comes from native modules
                 :keyUID "blablabla" ;; comes from native modules
                 :password "goooooodstuff"}) ;; comes from the UI

(defn connection-string-callback [response]
  (log/debug "we have a call back from status-go --->" response)
  )
;
;(defn get-connection-string
;  (status/get-connection-string-for-bootstrapping-another-device (.stringify js/JSON configJson) connection-string-callback))

(defn views []
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])]
;        _ (get-connection-string)]
    [:<>
     [rn/view {:style styles/body-container}
      [rn/text {:style styles/header-text} "Sync code generated"]
      [qr-code-viewer/qr-code-view (* window-width 0.808) example-key]
      [information-box/information-box {:type      :informative
                                        :closable? false
                                        :icon      :main-icons2/placeholder
                                        :style     {:margin-top 20}} "On your other device, navigate to the Syncing screen and select “Scan sync”"]]]))
