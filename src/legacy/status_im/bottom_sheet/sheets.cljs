(ns legacy.status-im.bottom-sheet.sheets
  (:require
    [legacy.status-im.bottom-sheet.view :as bottom-sheet]
    [legacy.status-im.ui.screens.about-app.views :as about-app]
    [legacy.status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
    [quo.theme]
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

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

          (= view :mobile-network-offline)
          (merge mobile-network-settings/offline-sheet)

          (= view :learn-more)
          (merge about-app/learn-more))]

    [:f>
     (fn []
       (rn/use-mount (fn []
                       (rn/hw-back-add-listener dismiss-bottom-sheet-callback)
                       (fn []
                         (rn/hw-back-remove-listener dismiss-bottom-sheet-callback))))
       [quo.theme/provider (or (:theme options))
        [bottom-sheet/bottom-sheet opts
         (when content
           [content (when options options)])]])]))
