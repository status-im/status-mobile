(ns status-im.contacts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
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
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar-with-search]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]
            [status-im.components.styles :refer [color-blue
                                                 create-icon
                                                 icon-search]]))

(def contacts-limit 50)

(defn toolbar-view [show-search?]
  (let [new-contact? (get-in platform-specific [:contacts :new-contact-in-toolbar?])
        actions      (if new-contact?
                       [(act/add #(dispatch [:navigate-to :new-contact]))])]
    (toolbar-with-search
     {:show-search?       show-search?
      :search-key         :contact-list
      :title              (label :t/contacts)
      :search-placeholder (label :t/search-for)
      :nav-action         (act/hamburger open-drawer)
      :actions            actions
      :on-search-submit   (fn [text]
                            (when-not (str/blank? text)
                              (dispatch [:set :contacts-filter #(let [name (-> (or (:name %) "")
                                                                               (str/lower-case))
                                                                      text (str/lower-case text)]
                                                                  (not= (.indexOf name text) -1))])
                              (dispatch [:set :contact-list-search-text text])
                              (dispatch [:navigate-to :contact-list-search-results])))})))

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
               ^{:key contact}
               [contact-view {:contact       contact
                              :extended?     true
                              :on-click      click-handler
                              :more-on-click nil}])
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

(defn contact-list [_]
  (let [peoples              (subscribe [:get-added-people-with-limit contacts-limit])
        dapps                (subscribe [:get-added-dapps-with-limit contacts-limit])
        people-count         (subscribe [:added-people-count])
        dapps-count          (subscribe [:added-dapps-count])
        click-handler        (subscribe [:get :contacts-click-handler])
        show-search          (subscribe [:get-in [:toolbar-search :show]])
        show-toolbar-shadow? (r/atom false)]
    (fn [current-view?]
      [view st/contacts-list-container
       [toolbar-view (and current-view?
                          (= @show-search :contact-list))]
       [view {:style st/toolbar-shadow}
        (when @show-toolbar-shadow?
          [linear-gradient {:style  st/contact-group-header-gradient-bottom
                            :colors st/contact-group-header-gradient-bottom-colors}])]
       (if (pos? (+ @dapps-count @people-count))
         [scroll-view {:style    st/contact-groups
                       :onScroll (fn [e]
                                   (let [offset (.. e -nativeEvent -contentOffset -y)]
                                     (reset! show-toolbar-shadow?
                                             (<= st/contact-group-header-height offset))))}
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
         [contacts-action-button])])))
