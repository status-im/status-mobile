(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.utils.fx :as fx]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.screens.navigation :as navigation]))

(if js/goog.DEBUG
  (.ignoreWarnings (.-YellowBox js-dependencies/react-native) #js ["re-frame: overwriting"])
  (aset js/console "disableYellowBox" true))

(fx/defn access-key-pressed
  {:events [:multiaccounts.recover.ui/recover-multiaccount-button-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :multiaccounts/recover)}
            (navigation/navigate-to-cofx :recover nil)))

(fx/defn set-supported-biometric-auth
  {:events [:init/app-started]}
  [{:keys [db]}]
  {:db {:view-id :intro :navigation-stack '(:intro)}})

(defn init [app-root]
  (re-frame/dispatch [:init/app-started])
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root)))