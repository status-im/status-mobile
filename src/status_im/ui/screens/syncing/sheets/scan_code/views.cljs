(ns status-im.ui.screens.syncing.sheets.scan-code.views
  (:require [clojure.string :as string]
            [quo.react-native :as rn]
            [status-im.ui.screens.syncing.sheets.sync-generated-code.styles :as styles]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [quo2.components.buttons.button :as quo2]
            [status-im.react-native.resources :as resources]))

(defn views []
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])]
    [:<>
     [rn/view {:style styles/body-container}
      [rn/text {:style styles/header-text} "Scan code"]]]))
