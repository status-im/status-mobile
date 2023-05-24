(ns status-im.bottom-sheet.sheets
  (:require [utils.re-frame :as rf]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.bottom-sheet.view :as bottom-sheet]
            [react-native.core :as rn]))

(defn bottom-sheet
  []
  (let [dismiss-bottom-sheet-callback (fn []
                                        (rf/dispatch [:bottom-sheet/hide-old])
                                        true)
        {:keys [show? view options]} (rf/sub [:bottom-sheet-old])
        {:keys [content]
         :as   opts}
        (cond-> {:visible? show?}
          (map? view)
          (merge view)

          (= view :mobile-network)
          (merge mobile-network-settings/settings-sheet)

          (= view :mobile-network-offline)
          (merge mobile-network-settings/offline-sheet)

          (= view :keycard.login/more)
          (merge keycard/more-sheet)

          (= view :learn-more)
          (merge about-app/learn-more))]

    [:f>
     (fn []
       (rn/use-effect (fn []
                        (rn/hw-back-add-listener dismiss-bottom-sheet-callback)
                        (fn []
                          (rn/hw-back-remove-listener dismiss-bottom-sheet-callback))))
       [bottom-sheet/bottom-sheet opts
        (when content
          [content (when options options)])])]))
