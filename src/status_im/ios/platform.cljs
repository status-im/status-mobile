(ns status-im.ios.platform
  (:require [status-im.components.styles :as styles]
            [status-im.utils.utils :as u]
            [reagent.core :as r]))

(def component-styles
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


;; Dialogs

(def react-native (u/require "react-native"))

(defn show-action-sheet [{:keys [options callback cancel-text]}]
  (.showActionSheetWithOptions (r/adapt-react-class (.-ActionSheetIOS react-native))
                               (clj->js {:options           (conj options cancel-text)
                                         :cancelButtonIndex (count options)})
                               callback))


;; Structure to be exported

(def platform-specific
  {:component-styles  component-styles
   :fonts             fonts
   :list-selection-fn show-action-sheet})

