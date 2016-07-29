(ns status-im.contacts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [view text
                                                image
                                                touchable-highlight
                                                linear-gradient
                                                scroll-view
                                                list-view
                                                list-item] :as react]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.contacts.views.contact :refer [contact-extended-view]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.components.styles :refer [color-blue
                                                 hamburger-icon
                                                 icon-search
                                                 create-icon
                                                 toolbar-background2]]
            [status-im.components.tabs.bottom-gradient :refer [bottom-gradient]]
            [status-im.contacts.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.components.styles :as cst]))

(defn contact-list-toolbar [platform-specific]
  [view
   [status-bar {:platform-specific platform-specific}]
   [toolbar {:nav-action       {:image   {:source {:uri :icon_hamburger}
                                          :style  hamburger-icon}
                                :handler open-drawer}
             :title            (label :t/contacts)
             :background-color toolbar-background2
             :style            {:elevation 0}
             :action           {:image   {:source {:uri :icon_search}
                                          :style  icon-search}
                                :handler (fn [])}}]])

(def contacts-limit 10)

(defn contact-group [contacts contacts-count title group top?]
  [view st/contact-group
   [view st/contact-group-header
    (when-not top?
      [linear-gradient {:style  st/contact-group-header-gradient-top
                        :colors st/contact-group-header-gradient-top-colors}])
    [view st/contact-group-header-inner
     [text {:style st/contact-group-text} title]
     [text {:style st/contact-group-size-text} (str contacts-count)]]
    [linear-gradient {:style  st/contact-group-header-gradient-bottom
                      :colors st/contact-group-header-gradient-bottom-colors}]]
   ;; todo what if there is no contacts, should we show some information
   ;; about this?
   [view {:flexDirection :column}
    (for [contact contacts]
      ;; TODO not imlemented: contact more button handler
      ^{:key contact} [contact-extended-view contact nil nil])]
   (when (= contacts-limit (count contacts))
     [view st/show-all
      [touchable-highlight {:on-press #(dispatch [:show-group-contacts group])}
       [text {:style st/show-all-text} (label :show-all)]]])])

(defn contact-list [{platform-specific :platform-specific}]
  (let [contacts             (subscribe [:get-contacts-with-limit contacts-limit])
        contcats-count       (subscribe [:contacts-count])
        show-toolbar-shadow? (r/atom false)]
    (fn []
      [view st/contacts-list-container
       [contact-list-toolbar platform-specific]
       [view {:style st/toolbar-shadow}
        (when @show-toolbar-shadow?
          [linear-gradient {:style  st/contact-group-header-gradient-bottom
                            :colors st/contact-group-header-gradient-bottom-colors}])]
       (if (pos? @contcats-count)
         [scroll-view {:style    st/contact-groups
                       :onScroll (fn [e]
                                   (let [offset (.. e -nativeEvent -contentOffset -y)]
                                     (reset! show-toolbar-shadow? (<= st/contact-group-header-height offset))))}
          ;; TODO not implemented: dapps and persons separation
          [contact-group
           @contacts
           @contcats-count
           (label :t/contacs-group-dapps)
           :dapps true]
          [contact-group
           @contacts
           @contcats-count
           (label :t/contacs-group-people)
           :people false]]
         [view st/empty-contact-groups
          [react/icon :group_big st/empty-contacts-icon]
          [text {:style st/empty-contacts-text} (label :t/no-contacts)]])
       [view st/buttons-container
        [action-button {:buttonColor color-blue
                        :offsetY     16
                        :offsetX     16}
         [action-button-item
          {:title       (label :t/new-contact)
           :buttonColor :#9b59b6
           :onPress     #(dispatch [:navigate-to :new-contact])}
          [icon {:name  :md-create
                 :style create-icon}]]]]
       [bottom-gradient]])))
