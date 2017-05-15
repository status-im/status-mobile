(ns status-im.network-settings.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.utils.listview :as lw]
            [re-frame.core :refer [dispatch]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.action-button.action-button :refer [action-button-view]]
            [status-im.components.action-button.styles :refer [actions-list]]
            [status-im.components.react :refer [view text icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.common.common :as common]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.network-settings.styles :as st]
            [status-im.i18n :as i18n]))

(defn network-icon [connected? size]
  [view (st/network-icon connected? size)
   [icon (if connected? :network_white :network_gray)]])

(defn network-badge [& [{:keys [name connected? options]}]]
  [view st/network-badge
   [network-icon connected? 56]
   [view {:padding-left 16}
    [text {:style st/badge-name-text}
     (or name (i18n/label :t/new-network))]
    (when connected?
      [text {:style st/badge-connected-text}
       (i18n/label :t/connected)])]])

(defn actions-view []
  [view actions-list
   [view {:opacity 0.4}
    [action-button-view (i18n/label :t/add-new-network) :add_blue]]
   #_[context-menu ; TODO should be implemented later
      [action-button-view (i18n/label :t/add-new-network) :add_blue]
      [{:text (i18n/label :t/add-json-file)      :value #(dispatch [:navigate-to :paste-json-text])}
       {:text (i18n/label :t/paste-json-as-text) :value #(dispatch [:navigate-to :paste-json-text])}
       {:text (i18n/label :t/specify-rpc-url)    :value #(dispatch [:navigate-to :add-rpc-url])}]]])

(defn render-row [current-network]
  (fn [{:keys [id name config] :as row} _ _]
    (let [connected? (= id current-network)]
      (list-item
        ^{:key row}
        [touchable-highlight {:on-press #(do
                                           (dispatch [:set :selected-network row])
                                           (dispatch [:navigate-to :network-details]))}
         [view st/network-item
          [network-icon connected? 40]
          [view {:padding-horizontal 16}
           [text {:style st/network-item-name-text}
            name]
           (when connected?
             [text {:style st/network-item-connected-text}
              (i18n/label :t/connected)])]]]))))

(defview network-settings []
  [{:keys [network networks]} [:get-current-account]]
  [view {:flex 1}
   [status-bar]
   [toolbar {:title (i18n/label :t/network-settings)}]
   [view {:flex 1}
    [list-view {:dataSource      (lw/to-datasource (vals networks))
                :renderRow       (render-row network)
                :renderHeader    #(list-item
                                    [view
                                     [actions-view]
                                     [common/bottom-shadow]
                                     [common/form-title (i18n/label :t/existing-networks)
                                      {:count-value (count networks)}]
                                     [common/list-header]])
                :renderFooter    #(list-item [view
                                              [common/list-footer]
                                              [common/bottom-shadow]])
                :renderSeparator renderers/list-separator-renderer
                :style           st/networks-list}]]])