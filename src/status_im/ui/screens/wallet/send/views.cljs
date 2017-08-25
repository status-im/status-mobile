(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.react :as rn]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.send.styles :as cst]))

(defn toolbar-title []
  [rn/view {:style cst/toolbar-title-container}
   [rn/text {:style cst/toolbar-title-text
             :font  :toolbar-title}
    "Send Transaction"]])

(defn toolbar-buttons []
  [rn/view {:style cst/toolbar-buttons-container}
   [vi/icon :icons/dots_vertical {:color :white}]
   [vi/icon :icons/qr {:color :white}]])

(defn toolbar-view []
  [toolbar/toolbar {:style          cst/toolbar
                    :custom-content [toolbar-title]
                    :custom-action  [toolbar-buttons]}])

(defview send-transaction []
  []
  [rn/view {:style cst/wallet-container}
   [toolbar-view]
   [rn/text "Nothing here yet!"]])
