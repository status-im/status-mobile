(ns status-im.contexts.communities.overview.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.home.actions.view :as actions]
    [status-im.common.resources :as resources]
    [status-im.common.scroll-page.style :as scroll-page.style]
    [status-im.common.scroll-page.view :as scroll-page]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.chat.view :as chat-actions]
    [status-im.contexts.communities.actions.community-options.view :as options]
    [status-im.contexts.communities.overview.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- add-category-height
  [categories-heights category height]
  (swap! categories-heights assoc category height))

(defn- collapse-category
  [community-id category-id collapsed?]
  (rf/dispatch [:communities/toggle-collapsed-category community-id category-id (not collapsed?)]))

(defn- layout-y
  [event]
  (oops/oget event "nativeEvent.layout.y"))

(defn- channel-chat-item
  [community-id
   {:keys [name emoji muted? id mentions-count unread-messages? on-press locked? color] :as chat}
   last-item?]
  (let [sheet-content      [actions/chat-actions
                            (assoc chat
                                   :community-id community-id
                                   :chat-type    constants/community-chat-type
                                   :chat-id      (str community-id id))
                            false]
        notification       (cond
                             muted?               :mute
                             (> mentions-count 0) :mention
                             unread-messages?     :notification
                             :else                nil)
        channel-options    {:name                name
                            :emoji               emoji
                            :customization-color color
                            :mentions-count      mentions-count
                            ;; NOTE: this is a troolean, nil/true/false have different meaning
                            :locked?             locked?
                            :notification        notification}
        channel-sheet-data {:selected-item (fn [] [quo/channel channel-options])
                            :content       (fn [] sheet-content)}]
    [rn/view {:key id :style (when last-item? {:margin-bottom 8})}
     [quo/channel
      (assoc channel-options
             :on-press      on-press
             :on-long-press #(rf/dispatch [:show-bottom-sheet channel-sheet-data]))]]))

(defn- channel-list-component
  [{:keys [on-category-layout community-id on-first-channel-height-changed]}
   channels-list]
  [rn/view
   {:on-layout #(on-first-channel-height-changed
                 (+ 38 (int (Math/ceil (layout-y %))))
                 (into #{} (map (comp :name second) channels-list)))
    :style     (style/channel-list-component)}
   (for [[category-id {:keys [chats name collapsed?]}] channels-list]
     (when (seq chats)
       [rn/view
        {:key       category-id
         ;; on-layout fires only when the component re-renders, so
         ;; in case the category hasn't changed, it will not be fired
         :on-layout #(on-category-layout name category-id (int (layout-y %)))}
        (when-not (= constants/empty-category-id category-id)
          [quo/divider-label
           {:on-press     #(collapse-category community-id category-id collapsed?)
            :chevron-icon (if collapsed? :i/chevron-right :i/chevron-down)
            :chevron      :left}
           name])
        (when-not collapsed?
          [rn/view {:style {:padding-horizontal 8}}
           (let [last-item-index (dec (count chats))]
             (map-indexed
              (fn [index chat]
                ^{:key (:id chat)}
                [channel-chat-item community-id chat (= index last-item-index)])
              chats))])]))])

(defn- get-access-type
  [access]
  (condp = access
    constants/community-no-membership-access   :open
    constants/community-invitation-only-access :invite-only
    constants/community-on-request-access      :request-access
    :unknown-access))

(defn- network-not-supported
  []
  [quo/text (i18n/label :t/network-not-supported)])

(defn- request-access-button
  [id color]
  [quo/button
   {:on-press            (if config/community-accounts-selection-enabled?
                           #(rf/dispatch [:open-modal :community-account-selection-sheet
                                          {:community-id id}])
                           #(rf/dispatch [:open-modal :community-requests-to-join {:id id}]))
    :accessibility-label :show-request-to-join-screen-button
    :customization-color color
    :container-style     {:margin-bottom 12}
    :icon-left           :i/communities}
   (i18n/label :t/request-to-join)])

(defn token-gated-communities-info
  []
  [quo/documentation-drawers {:title (i18n/label :t/token-gated-communities)}
   [quo/text {:size :paragraph-2}
    (i18n/label :t/token-gated-communities-info)]])

(defn- token-requirements
  [{:keys [id color role-permissions?]}]
  (let [{:keys [can-request-access? no-member-permission? networks-not-supported?
                highest-permission-role
                tokens]}  (rf/sub [:community/token-gated-overview id])
        highest-role-text (i18n/label
                           (communities.utils/role->translation-key highest-permission-role :t/member))
        on-press          (rn/use-callback
                           (fn []
                             (if config/community-accounts-selection-enabled?
                               (rf/dispatch [:open-modal :community-account-selection-sheet
                                             {:community-id id}])
                               (rf/dispatch [:open-modal :community-requests-to-join
                                             {:id id}])))
                           [id])
        on-press-info     #(rf/dispatch
                            [:show-bottom-sheet {:content token-gated-communities-info}])]
    (cond
      networks-not-supported?
      [network-not-supported]

      (or (not role-permissions?) no-member-permission?)
      [request-access-button id color]

      :else
      [quo/community-token-gating
       {:role            highest-role-text
        :tokens          tokens
        :community-color color
        :satisfied?      can-request-access?
        :on-press        on-press
        :on-press-info   on-press-info}])))

(defn- join-community
  [{:keys [id joined permissions] :as community}]
  (let [pending?        (rf/sub [:communities/my-pending-request-to-join id])
        access-type     (get-access-type (:access permissions))
        unknown-access? (= access-type :unknown-access)
        invite-only?    (= access-type :invite-only)]
    (when-not (or joined pending? invite-only? unknown-access?)
      [token-requirements community])))

(defn- status-tag
  [community-id joined]
  (let [pending? (rf/sub [:communities/my-pending-request-to-join community-id])]
    (when (or pending? joined)
      [rn/view {:style {:position :absolute :top 12 :right 12}}
       [quo/status-tag
        {:status {:type (if joined :positive :pending)}
         :label  (if joined
                   (i18n/label :t/joined)
                   (i18n/label :t/pending))}]])))

(defn- add-handlers
  [community-id
   joined-or-spectated
   {:keys [id locked?]
    :or   {locked? false}
    :as   chat}]
  (cond-> chat
    (and (not locked?) id)
    (assoc :on-press      (when joined-or-spectated
                            (fn []
                              (rf/dispatch [:dismiss-keyboard])
                              (debounce/throttle-and-dispatch
                               [:communities/navigate-to-community-chat (str community-id id)]
                               1000)))
           :on-long-press #(rf/dispatch
                            [:show-bottom-sheet
                             {:content (fn []
                                         [chat-actions/actions chat false])}])
           :community-id  community-id)))

(defn- add-handlers-to-chats
  [community-id joined-or-spectated chats]
  (mapv (partial add-handlers community-id joined-or-spectated) chats))

(defn- add-handlers-to-categorized-chats
  [community-id categorized-chats joined-or-spectated]
  (let [add-on-press (partial add-handlers-to-chats community-id joined-or-spectated)]
    (map (fn [[category v]]
           [category (update v :chats add-on-press)])
         categorized-chats)))

(defn- community-header
  [title logo description]
  [quo/text-combinations
   {:container-style                 {:margin-top
                                      (if logo
                                        12
                                        (+ scroll-page.style/picture-radius
                                           scroll-page.style/picture-border-width
                                           12))
                                      :margin-bottom 12}
    :avatar                          logo
    :title                           title
    :title-number-of-lines           2
    :description                     description
    :title-accessibility-label       :community-title
    :description-accessibility-label :community-description}])

(defn- community-content
  [_]
  (fn [id
       {:keys [on-category-layout
               collapsed?
               on-first-channel-height-changed]}]
    (let [{:keys [name description joined spectated images tags id membership-permissions?]
           :as   community}
          (rf/sub [:communities/community id])
          joined-or-spectated (or joined spectated)
          chats-by-category (rf/sub [:communities/categorized-channels id])]
      [:<>
       [rn/view {:style style/community-content-container}
        (when-not collapsed?
          [status-tag id joined])
        [community-header name (when collapsed? (get-in images [:thumbnail :uri]))
         (when-not collapsed? description)]
        (when (and (seq tags) (not collapsed?))
          [quo/community-tags
           {:tags            tags
            :last-item-style style/last-community-tag
            :container-style style/community-tag-container}])
        [join-community community]]
       (when (or joined (not membership-permissions?))
         [channel-list-component
          {:on-category-layout              on-category-layout
           :community-id                    id
           :on-first-channel-height-changed on-first-channel-height-changed}
          (add-handlers-to-categorized-chats id chats-by-category joined-or-spectated)])])))

(defn- sticky-category-header
  [_]
  (fn [{:keys [enabled label]}]
    (when enabled
      [quo/blur
       {:style         style/blur-channel-header
        :blur-amount   20
        :blur-type     :transparent
        :overlay-color :transparent}
       [quo/divider-label
        {:chevron :left}
        label]])))

(defn- page-nav-right-section-buttons
  [id]
  [{:icon-name           :i/options
    :accessibility-label :community-options-for-community
    :on-press            #(rf/dispatch
                           [:show-bottom-sheet
                            {:content (fn [] [options/community-options-bottom-sheet id])}])}])

(defn- pick-first-category-by-height
  [scroll-height first-channel-height categories-heights]
  (->> categories-heights
       (sort-by (comp - second))
       (some (fn [[category height]]
               (and (>= scroll-height (+ height first-channel-height))
                    category)))))

        ;; We track the initial value of joined
        ;; as we open the page to avoid switching
        ;; from not collapsed to collapsed if the
        ;; user is on this page

(defn- community-scroll-page
  [_ initial-joined? _ _]
  (let [scroll-height                   (reagent/atom 0)
        categories-heights              (reagent/atom {})
        first-channel-height            (reagent/atom 0)
        on-category-layout              (partial add-category-height categories-heights)
        on-first-channel-height-changed (fn [height categories]
                                          (swap! categories-heights select-keys categories)
                                          (reset! first-channel-height height))]
    (fn [id joined name images]
      (let [theme                 (quo.theme/use-theme)
            cover                 {:uri (get-in images [:banner :uri])}
            logo                  {:uri (get-in images [:large :uri])}
            collapsed?            (and initial-joined? joined)
            first-category-height (->> @categories-heights
                                       vals
                                       (apply min)
                                       (+ @first-channel-height))
            overlay-shown?        (boolean (:sheets (rf/sub [:bottom-sheet])))]
        [scroll-page/scroll-page
         {:cover-image      cover
          :collapsed?       collapsed?
          :logo             logo
          :name             name
          :on-scroll        #(reset! scroll-height %)
          :navigate-back?   true
          :height           148
          :overlay-shown?   overlay-shown?
          :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
          :page-nav-props   {:type           :community
                             :right-side     (page-nav-right-section-buttons id)
                             :community-name name
                             :community-logo logo}
          :sticky-header    [sticky-category-header
                             {:enabled (> @scroll-height
                                          first-category-height)
                              :label   (pick-first-category-by-height
                                        @scroll-height
                                        @first-channel-height
                                        @categories-heights)}]}
         [community-content
          id
          {:on-category-layout              on-category-layout
           :collapsed?                      collapsed?
           :on-first-channel-height-changed
           ;; Here we set the height of the component and we filter out the categories, as some
           ;; might have been removed.
           on-first-channel-height-changed}]]))))

(defn- community-fetching-placeholder
  [id]
  (let [theme     (quo.theme/use-theme)
        top-inset (safe-area/get-top)
        fetching? (rf/sub [:communities/fetching-community id])]
    [rn/view
     {:style               (style/fetching-placeholder top-inset)
      :accessibility-label (if fetching?
                             :fetching-community-overview
                             :failed-to-fetch-community-overview)}
     [quo/page-nav
      {:title      (i18n/label :t/community-overview)
       :type       :title
       :text-align :left
       :icon-name  :i/close
       :on-press   #(rf/dispatch [:navigate-back])}]
     [quo/empty-state
      {:image           (resources/get-themed-image :cat-in-box theme)
       :description     (when-not fetching? (i18n/label :t/here-is-a-cat-in-a-box-instead))
       :title           (if fetching?
                          (i18n/label :t/fetching-community)
                          (i18n/label :t/failed-to-fetch-community))
       :container-style {:flex 1 :justify-content :center}}]]))

(defn- community-card-page-view
  [id]
  (let [{:keys [joined name images]
         :as   community} (rf/sub [:communities/community id])]
    (if community
      [community-scroll-page id joined name images]
      [community-fetching-placeholder id])))

(defn view
  [id]
  (let [id (or id (rf/sub [:get-screen-params :community-overview]))]
    [rn/view {:style style/community-overview-container}
     [community-card-page-view id]]))
