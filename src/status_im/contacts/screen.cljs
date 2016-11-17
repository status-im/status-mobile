(ns status-im.contacts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight
                                                linear-gradient
                                                scroll-view
                                                list-view
                                                list-item] :as react]
            [status-im.components.action-button :refer [action-button
                                                        action-button-item]]
            [status-im.contacts.views.contact :refer [contact-extended-view on-press]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.styles :refer [toolbar-background2]]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.styles :refer [color-blue
                                                 hamburger-icon
                                                 icon-search
                                                 create-icon]]
            [status-im.components.tabs.bottom-gradient :refer [bottom-gradient]]
            [status-im.contacts.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]))

(def contacts-limit 50)

(defn toolbar-view []
  (let [new-contact? (get-in platform-specific [:contacts :new-contact-in-toolbar?])
        actions      (cond->> [{:image   {:source {:uri :icon_search}
                                          :style  icon-search}
                                :handler (fn [])}]
                              new-contact?
                              (into [{:image   {:source {:uri :icon_add}
                                                :style  icon-search}
                                      :handler #(dispatch [:navigate-to :new-contact])}]))]
    [toolbar {:nav-action       {:image   {:source {:uri :icon_hamburger}
                                           :style  hamburger-icon}
                                 :handler open-drawer}
              :title            (label :t/contacts)
              :background-color toolbar-background2
              :style            {:elevation 0}
              :actions          actions}]))

(defn subtitle-view [subtitle contacts-count]
  [view st/contact-group-header-inner
   [text {:style      (merge st/contact-group-subtitle
                             (get-in platform-specific [:component-styles :contacts :subtitle]))
          :uppercase? (get-in platform-specific [:contacts :uppercase-subtitles?])
          :font       :medium}
    subtitle]
   [text {:style      (merge st/contact-group-count
                             (get-in platform-specific [:component-styles :contacts :subtitle]))
          :uppercase? (get-in platform-specific [:contacts :uppercase-subtitles?])
          :font       :medium}
    (str contacts-count)]])

(defn group-top-view []
  [linear-gradient {:style  st/contact-group-header-gradient-bottom
                    :colors st/contact-group-header-gradient-bottom-colors}])

(defn group-bottom-view []
  [linear-gradient {:style  st/contact-group-header-gradient-top
                    :colors st/contact-group-header-gradient-top-colors}])

(defn line-view []
  [view {:style {:background-color "#D7D7D7"
                 :height           1}}])

(defn contact-group-view [contacts contacts-count subtitle group click-handler]
  (let [shadows? (get-in platform-specific [:contacts :group-block-shadows?])]
    [view st/contact-group
     [view st/contact-group-header
      [subtitle-view subtitle contacts-count]]
     (if shadows?
       [group-top-view]
       [line-view])
     [view
      (doall
        (map (fn [contact]
               (let [whisper-identity (:whisper-identity contact)
                     click-handler    (or click-handler on-press)]
                 ^{:key contact}
                 [contact-extended-view contact nil (click-handler whisper-identity) nil]))
             contacts))]
     (when (<= contacts-limit (count contacts))
       [view st/show-all
        [touchable-highlight {:on-press #(dispatch [:navigate-to :group-contacts group])}
         [view
          [text {:style st/show-all-text
                 :font  :medium}
           (label :t/show-all)]]]])
     (if shadows?
       [group-bottom-view]
       [line-view])]))

(defn contacts-action-button []
  [view st/buttons-container
   [action-button {:button-color color-blue
                   :offset-x     16
                   :offset-y     -2
                   :hide-shadow  true
                   :spacing      13}
    [action-button-item
     {:title       (label :t/new-contact)
      :buttonColor :#9b59b6
      :onPress     #(dispatch [:navigate-to :new-contact])}
     [ion-icon {:name  :md-create
                :style create-icon}]]]])

(defn contact-list []
  (let [peoples              (subscribe [:get-added-people-with-limit contacts-limit])
        dapps                (subscribe [:get-added-dapps-with-limit contacts-limit])
        people-count         (subscribe [:added-people-count])
        dapps-count          (subscribe [:added-dapps-count])
        click-handler        (subscribe [:get :contacts-click-handler])
        show-toolbar-shadow? (r/atom false)]
    (fn []
      [view st/contacts-list-container
       [toolbar-view]
       [view {:style st/toolbar-shadow}
        (when @show-toolbar-shadow?
          [linear-gradient {:style  st/contact-group-header-gradient-bottom
                            :colors st/contact-group-header-gradient-bottom-colors}])]
       (if (pos? (+ @dapps-count @people-count))
         [scroll-view {:style    st/contact-groups
                       :onScroll (fn [e]
                                   (let [offset (.. e -nativeEvent -contentOffset -y)]
                                     (reset! show-toolbar-shadow? (<= st/contact-group-header-height offset))))}
          (when (pos? @dapps-count)
            [contact-group-view
             @dapps
             @dapps-count
             (label :t/contacts-group-dapps)
             :dapps
             @click-handler])
          (when (pos? @people-count)
            [contact-group-view
             @peoples
             @people-count
             (label :t/contacts-group-people)
             :people
             @click-handler])]
         [view st/empty-contact-groups
          [react/icon :group_big st/empty-contacts-icon]
          [text {:style st/empty-contacts-text} (label :t/no-contacts)]])
       (when (get-in platform-specific [:contacts :action-button?])
         [contacts-action-button])
       [bottom-gradient]])))
