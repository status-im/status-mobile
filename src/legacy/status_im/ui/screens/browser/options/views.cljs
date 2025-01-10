(ns legacy.status-im.ui.screens.browser.options.views
  (:require
    [legacy.status-im.browser.core :as browser]
    [legacy.status-im.qr-scanner.core :as qr-scanner]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn browser-options
  [url _account empty-tab name]
  (fn []
    (let [bookmarks @(re-frame/subscribe [:bookmarks/active])
          fav?      (get bookmarks url)]
      [react/view {:flex 1}
       [quo/button
        {:style               {:align-self   :flex-end
                               :margin-right 15}
         :type                :icon
         :theme               :icon
         :accessibility-label :universal-qr-scanner
         :on-press            #(hide-sheet-and-dispatch
                                [::qr-scanner/scan-code
                                 {:handler ::qr-scanner/on-scan-success}])}
        :main-icons/qr]
       (when-not empty-tab
         [:<>
          [list.item/list-item
           {:theme               :accent
            :title               (i18n/label :t/new-tab)
            :accessibility-label :new-tab
            :icon                :main-icons/add
            :on-press            #(hide-sheet-and-dispatch [:browser.ui/open-empty-tab])}]
          [list.item/list-item
           {:theme               :accent
            :title               (if fav? (i18n/label :t/remove-favourite) (i18n/label :t/add-favourite))
            :accessibility-label :add-remove-fav
            :icon                (if fav? :main-icons/delete :main-icons/favourite)
            :on-press            #(hide-sheet-and-dispatch
                                   (if fav?
                                     [:browser/delete-bookmark url]
                                     [:open-modal :new-bookmark {:url url :name name :new true}]))}]
          [list.item/list-item
           {:theme               :accent
            :title               (i18n/label :t/share)
            :accessibility-label :share
            :icon                :main-icons/share
            :on-press            (fn []
                                   (re-frame/dispatch-sync [:bottom-sheet/hide-old])
                                   (js/setTimeout
                                    #(browser/share-link url)
                                    200))}]
          [react/view {:style {:height 1 :background-color (colors/alpha colors/black 0.1)}}]])
      ])))
