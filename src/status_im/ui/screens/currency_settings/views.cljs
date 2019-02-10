(ns status-im.ui.screens.currency-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.currency-settings.styles :as styles]
            [status-im.constants :as constants]))

(defn render-currency [current-currency-id]
  (fn [{:keys [id code display-name] :as currency}]
    (let [selected? (= id current-currency-id)]
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:wallet.settings.ui/currency-selected id])
        :accessibility-label :currency-item}
       [react/view styles/currency-item
        [react/text {:style styles/currency-name-text}
         (str display-name " (" code ")")]
        (when selected?
          [vector-icons/icon :main-icons/check {:color :active}])]])))

(views/defview currency-settings []
  (views/letsubs [currency-id [:wallet.settings/currency]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/main-currency)]
     [react/view styles/wrapper
      [list/flat-list {:data      (->> constants/currencies
                                       vals
                                       (sort #(compare (:code %1) (:code %2))))
                       :key-fn    :code
                       :separator (profile.components/settings-item-separator)
                       :render-fn (render-currency currency-id)}]]]))
