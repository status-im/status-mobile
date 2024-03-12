(ns status-im.contexts.preview.status-im.banners.alert-banner
  (:require
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [utils.re-frame :as rf]))

(def descriptor
  [{:key  :alert-banner?
    :type :boolean}
   {:key  :alert-message
    :type :text}
   {:key  :error-banner?
    :type :boolean}
   {:key  :error-message
    :type :text}])

(defn view
  []
  (let [state         (reagent/atom {:alert-banner? false
                                     :alert-message "Testnet mode enabled"
                                     :error-banner? false
                                     :error-message "Main and failover JSON RPC URLs offline"})
        alert-banner? (reagent/cursor state [:alert-banner?])
        alert-message (reagent/cursor state [:alert-message])
        error-banner? (reagent/cursor state [:error-banner?])
        error-message (reagent/cursor state [:error-message])]
    (fn []
      (if @alert-banner?
        (rf/dispatch [:alert-banners/add
                      {:type :alert
                       :text @alert-message}])
        (rf/dispatch [:alert-banners/remove :alert]))
      (if @error-banner?
        (rf/dispatch [:alert-banners/add
                      {:type :error
                       :text @error-message}])
        (rf/dispatch [:alert-banners/remove :error]))
      [preview/preview-container {:state state :descriptor descriptor}])))
