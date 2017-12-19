(ns status-im.ui.screens.accounts.recover.styles
  (:require [status-im.ui.components.styles :as common]
            [status-im.utils.platform :refer [ios?]]))

(def screen-container
  {:flex             1
   :background-color common/color-white})

(def passphrase-input-max-height
  (if ios? 78 72))
