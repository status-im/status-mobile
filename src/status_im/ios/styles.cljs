(ns status-im.ios.styles
  (:require [status-im.components.styles :as styles]))

(def components
  {:status-bar        {:default     {:height    20
                                     :bar-style "default"
                                     :color     styles/color-white}
                       :transparent {:height    20
                                     :bar-style "light-content"
                                     :color     styles/color-transparent}}
   :toolbar           {:border-bottom-color styles/color-gray3
                       :border-bottom-width 0.5}
   :actions-list-view {:border-bottom-color styles/color-gray3
                       :border-bottom-width 0.5}
   :chat              {:new-message {:border-top-color styles/color-gray3
                                     :border-top-width 0.5}}})

(def fonts
  {:default {:font-family "SFUIDisplay-Regular"}
   :medium  {:font-family "SFUIDisplay-Medium"}})

(def styles
  {:components components
   :fonts      fonts})