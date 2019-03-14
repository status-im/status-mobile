(ns status-im.ui.screens.wallet.transaction-sent.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(def transaction-sent
  {:typography    :header
   :color         colors/white
   :margin-bottom 8})

(def transaction-sent-description
  {:typography         :title
   :color              (colors/alpha colors/white 0.6)
   :text-align         :center
   :padding-horizontal 30
   :margin-bottom      35})
