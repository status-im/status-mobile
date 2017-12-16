(ns status-im.ios.platform
  (:require [status-im.i18n :refer [label]]
            [status-im.utils.utils :as utils]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def fonts
  {:light         {:font-family "SFUIText-Light"}
   :default       {:font-family "SFUIText-Regular"}
   :medium        {:font-family "SFUIText-Medium"}
   :bold          {:font-family "SFUIText-Bold"}

   :toolbar-title {:font-family "SFUIText-Semibold"}
   :roboto-mono   {:font-family "RobotoMono-Medium"}})

;; Dialogs

(defn action-sheet-options [options]
  (let [destructive-opt-index (utils/first-index :destructive? options)
        cancel-option         {:text (label :t/cancel)}
        options               (conj options cancel-option)]
    (clj->js (merge {:options           (mapv :text options)
                     :cancelButtonIndex (dec (count options))}
                    (when destructive-opt-index {:destructiveButtonIndex destructive-opt-index})))))

(defn show-action-sheet [{:keys [options callback]}]
  (.showActionSheetWithOptions (.-ActionSheetIOS rn-dependencies/react-native)
                               (action-sheet-options options)
                               callback))

;; Structure to be exported

(def platform-specific
  {:fonts                        fonts
   :list-selection-fn            show-action-sheet
   :tabs                         {:tab-shadows? false}
   :chats                        {:action-button?       false
                                  :new-chat-in-toolbar? true
                                  :render-separator?    true}
   :uppercase?                   false
   :contacts                     {:action-button?          false
                                  :new-contact-in-toolbar? true}
   :group-block-shadows?         false
   :discover                     {:uppercase-subtitles? true}
   :status-bar-default-height    20})
