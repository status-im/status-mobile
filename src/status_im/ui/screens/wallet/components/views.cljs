(ns status-im.ui.screens.wallet.components.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.components.styles :as styles]))

(defn separator []
  [react/view (styles/separator)])