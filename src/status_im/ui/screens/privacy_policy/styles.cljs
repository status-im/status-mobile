(ns status-im.ui.screens.privacy-policy.styles
  (:require [status-im.ui.components.common.styles :as common-styles]
            [status-im.ui.components.colors :as colors]))

(def privacy-policy-button-container
  {:margin-bottom 16
   :margin-top    42})

(def privacy-policy-button-text
  (merge common-styles/button-label
         {:font-size 14}))

(def privacy-policy-button-text-gray
  (merge privacy-policy-button-text {:color colors/gray}))
