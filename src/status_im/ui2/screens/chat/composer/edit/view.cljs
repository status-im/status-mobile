(ns status-im.ui2.screens.chat.composer.edit.view
  (:require [react-native.core :as rn]
            [status-im.ui2.screens.chat.components.edit.view :as edit]
            [status-im.ui2.screens.chat.composer.edit.style :as style]
            [status-im.ui2.screens.chat.composer.input :as input]))

(defn- focus-input-on-edit
  [edit had-edit text-input-ref]
  ;;when we show edit we focus input
  (when-not (= edit @had-edit)
    (reset! had-edit edit)
    (when edit
      ;; A setTimeout of 0 is necessary to ensure the statement is enqueued and will get executed ASAP.
      (js/setTimeout #(input/input-focus text-input-ref) 0))))

(defn edit-message-auto-focus-wrapper
  [text-input-ref _]
  (let [had-edit (atom nil)]
    (fn [_ edit on-cancel]
      (focus-input-on-edit edit had-edit text-input-ref)
      (when edit
        [rn/view {:style style/container}
         [edit/edit-message on-cancel]]))))
