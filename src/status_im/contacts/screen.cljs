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
                                                list-item]]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.contacts.views.contact :refer [contact-extended-view]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.components.styles :refer [color-blue
                                                 hamburger-icon
                                                 icon-search
                                                 create-icon
                                                 toolbar-background2]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defn contact-list-toolbar []
  [toolbar {:nav-action       {:image   {:source {:uri :icon_hamburger}
                                         :style  hamburger-icon}
                               :handler open-drawer}
            :title            (label :t/contacts)
            :background-color toolbar-background2
            :style            {:elevation 0}
            :action           {:image   {:source {:uri :icon_search}
                                         :style  icon-search}
                               :handler (fn [])}}])

(defn contact-group [contacts title group top?]
  [view st/contact-group
   [view st/contact-group-header
    (when-not top?
      [linear-gradient {:style  st/contact-group-header-gradient-top
                        :colors st/contact-group-header-gradient-top-colors}])
    [view st/contact-group-header-inner
     [text {:style st/contact-group-text} title]
     [text {:style st/contact-group-size-text} (str (count contacts))]]
    [linear-gradient {:style  st/contact-group-header-gradient-bottom
                      :colors st/contact-group-header-gradient-bottom-colors}]]
   ;; todo what if there is no contacts, should we show some information
   ;; about this?
   (when contacts
     [view {:flexDirection :column}
      (for [contact (take 4 contacts)]
        ;; TODO not imlemented: contact more button handler
        ^{:key contact} [contact-extended-view contact nil nil])])
   [view st/show-all
    [touchable-highlight {:on-press #(dispatch [:show-group-contacts group])}
     [text {:style st/show-all-text} (label :show-all)]]]])

(defn contact-list []
  (let [contacts (subscribe [:all-contacts])
        show-toolbar-shadow? (r/atom false)]
    (fn []
      [drawer-view
       [view st/contacts-list-container
        [contact-list-toolbar]
        [view {:style st/toolbar-shadow}
         (when @show-toolbar-shadow?
           [linear-gradient {:style  st/contact-group-header-gradient-bottom
                             :colors st/contact-group-header-gradient-bottom-colors}])]
        [scroll-view {:style    st/contact-groups
                      :onScroll (fn [e]
                                  (let [offset (.. e -nativeEvent -contentOffset -y)]
                                    (reset! show-toolbar-shadow? (<= st/contact-group-header-height offset))))}
         ;; TODO not implemented: dapps and persons separation
         [contact-group @contacts (label :contacs-group-dapps) :dapps true]
         [contact-group @contacts (label :contacs-group-people) :people false]]
        [action-button {:buttonColor color-blue
                        :offsetY     16
                        :offsetX     16}
         [action-button-item
          {:title       (label :t/new-contact)
           :buttonColor :#9b59b6
           :onPress     #(dispatch [:navigate-to :new-contact])}
          [icon {:name  :android-create
                 :style create-icon}]]]]])))
