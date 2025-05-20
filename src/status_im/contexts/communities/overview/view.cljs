(ns status-im.contexts.communities.overview.view
  (:require
    [oops.core :as oops]
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.events-helper :as events.helper]
    [status-im.common.home.actions.view :as actions]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.community-options.view :as options]
    [status-im.contexts.communities.overview.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.communities :as worklets]))

(def snap-header-threshold-factor
  "Threshold to automatically move the header to a collapsed/expanded state and avoid an
  intermediate state. Applied to `collapse-threshold`."
  0.65)

(def navbar-content-threshold-factor
  "When the community name and logo start to appear. Applied to sheet-displacement-threshold."
  32)

(def info-opacity-threshold-factor
  "Dragging distance to appear/disappear the community info (description, tags & stats)."
  0.5)

(defn- collapse-category
  [community-id category-id collapsed?]
  (rf/dispatch
   [:communities/toggle-collapsed-category community-id category-id (not collapsed?)]))

(defn- get-access-type
  [access]
  (condp = access
    constants/community-no-membership-access   :open
    constants/community-invitation-only-access :invite-only
    constants/community-on-request-access      :request-access
    :unknown-access))

(defn- show-join-modal
  [community-id]
  (fn []
    (rf/dispatch [:open-modal :screen/community-account-selection-sheet
                  {:community-id community-id}])))

(defn- promotional-info-box-for-owners
  [{:keys [theme info-styles]}]
  (let [[dismissed? set-dismissed] (rn/use-state false)]
    (when-not dismissed?
      [reanimated/view {:style [style/promote-community info-styles]}
       [quo/information-box
        {:type              :informative
         :closed?           false
         :on-close          #(set-dismissed true)
         :theme             theme
         :button-label      (i18n/label :t/initiate-the-vote)
         :button-icon-right :i/external
         :on-button-press   (fn []
                              (rf/dispatch [:browser.ui/open-url constants/community-vote-help-url]))}
        (i18n/label :t/help-discover-your-community)]])))

(defn token-gated-communities-info
  []
  [quo/documentation-drawers {:title (i18n/label :t/token-gated-communities)}
   [quo/text {:size :paragraph-2}
    (i18n/label :t/token-gated-communities-info)]])

(defn- on-join-info-press
  []
  (rf/dispatch [:show-bottom-sheet {:content token-gated-communities-info}]))

(defn- token-requirements
  [community-id role-permissions? color tags?]
  (let [{:keys [can-request-access? no-member-permission? networks-not-supported?
                highest-permission-role
                tokens]}        (rf/sub [:community/token-gated-overview community-id])
        on-request-access-press (show-join-modal community-id)]
    (cond
      networks-not-supported?
      [rn/view {:style (style/request-to-join-button tags?)}
       [quo/text (i18n/label :t/network-not-supported)]]

      (or (not role-permissions?) no-member-permission?)
      [rn/view {:style (style/request-to-join-button tags?)}
       [quo/button
        {:on-press            on-request-access-press
         :accessibility-label :show-request-to-join-screen-button
         :customization-color color
         :icon-left           :i/communities}
        (i18n/label :t/request-to-join)]]

      :else
      (let [highest-role-text (i18n/label
                               (communities.utils/role->translation-key
                                highest-permission-role
                                :t/member))]
        [rn/view {:style style/request-to-join-as}
         [quo/community-token-gating
          {:role            highest-role-text
           :tokens          tokens
           :community-color color
           :satisfied?      can-request-access?
           :on-press        on-request-access-press
           :on-press-info   on-join-info-press}]]))))

(defn- join-community
  [{:keys [community-id joined? permissions role-permissions? color tags?]}]
  (let [pending?    (rf/sub [:communities/my-pending-request-to-join community-id])
        access-type (get-access-type (:access permissions))]
    (when-not (or joined? pending? (#{:unknown-access :invite-only} access-type))
      [token-requirements community-id role-permissions? color tags?])))

(defn- status-tag
  [{:keys [community-id joined? info-styles]}]
  (let [pending? (rf/sub [:communities/my-pending-request-to-join community-id])]
    (when (or pending? joined?)
      [reanimated/view {:style [style/status-tag-position info-styles]}
       [quo/status-tag
        {:status {:type (if joined? :positive :pending)}
         :label  (if joined? (i18n/label :t/joined) (i18n/label :t/pending))}]])))

(defn- category-divider
  [community-id
   {collapsed?    :collapsed?
    category-name :name
    category-id   :id}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:style         (style/category-divider theme)
      :blur-amount   20
      :blur-type     :transparent
      :overlay-color :transparent}
     [quo/divider-label
      {:on-press     (fn [] (collapse-category community-id category-id collapsed?))
       :chevron-icon (if collapsed? :i/chevron-right :i/chevron-down)
       :chevron      :left
       :blur?        true}
      category-name]]))

(defn- notification-type
  [{:keys [muted? mentions-count unread-messages?] :as _chat}]
  (cond
    muted?               :mute
    (> mentions-count 0) :mention
    unread-messages?     :notification))

(defn- navigate-to-chat
  [chat-id]
  (rf/dispatch [:dismiss-keyboard])
  (debounce/throttle-and-dispatch
   [:communities/navigate-to-community-chat chat-id]
   1000))

(defn- show-chat-actions
  [chat-data]
  (rf/dispatch
   [:show-bottom-sheet
    {:selected-item (fn [] [quo/channel chat-data])
     :content       (fn [] [actions/chat-actions chat-data false])}]))

(defn channel-item
  [{:keys      [color locked?]
    channel-id :id
    :as        chat}
   {:keys [community-id joined-or-spectated?] :as _community-data}]
  [rn/view {:style {:padding-horizontal 8}}
   (let [chat-id       (str community-id channel-id)
         chat-data     (assoc chat
                              :community-id        community-id
                              :chat-type           constants/community-chat-type
                              :chat-id             chat-id
                              :customization-color color
                              :notification        (notification-type chat))
         on-press      (rn/use-callback
                        (fn []
                          (when (and (not locked?) channel-id joined-or-spectated?)
                            (navigate-to-chat chat-id)))
                        [joined-or-spectated? locked? channel-id])
         on-long-press (rn/use-callback
                        (fn []
                          (when (and (not locked?) channel-id)
                            (show-chat-actions chat-data)))
                        [locked? channel-id])]
     [quo/channel
      (assoc chat-data
             :on-press      on-press
             :on-long-press on-long-press)])])

(defn channel-listing-item
  [{:keys [community-id] :as community-data}]
  (fn [{:keys [render-as] :as item-data} _ _ _]
    (case render-as
      :separator [rn/view {:style {:height 8}}]
      :category  [category-divider community-id item-data]
      :channel   [channel-item item-data community-data]
      nil)))

(def channel-component-heights
  {:category  34
   :channel   48
   :separator 8})

(def unusable-area-height
  ;;top page buttons, button's padding & safe area, on Android we count page-nav top
  ;; because it isn't overlapped with the safe-area.
  (+ 32 11 safe-area/bottom (when platform/android? 12)))

(defn- calc-scrollable-content
  [scrollable-height]
  (- scrollable-height
     (- (:height safe-area/window) unusable-area-height)))

(defn- calc-listing-height
  []
  (+ (- (:height safe-area/window) unusable-area-height)
     safe-area/bottom))

(defn- channel-listing
  [{:keys [community-id scroll-amount header-height set-max-scroll collapse-threshold
           sheet-displacement-threshold expand-header-limit]}]
  (let [theme                (quo.context/use-theme)
        channels-styles      (worklets/use-channels-styles
                              {:scroll-amount                scroll-amount
                               :header-height                header-height
                               :collapse-threshold           collapse-threshold
                               :sheet-displacement-threshold sheet-displacement-threshold
                               :expand-header-limit          expand-header-limit})
        flat-list-ref        (reanimated/use-animated-ref)
        _scroll-to-animation (worklets/use-scroll-to
                              {:animated-ref        flat-list-ref
                               :scroll-amount       scroll-amount
                               :expand-header-limit expand-header-limit})
        {:keys [joined?
                spectated?]} (rf/sub [:communities/community-overview community-id])
        joined-or-spectated? (or joined? spectated?)
        render-fn            (rn/use-callback
                              (channel-listing-item {:community-id         community-id
                                                     :joined-or-spectated? joined-or-spectated?})
                              [joined-or-spectated?])
        flatten-channels     (rf/sub [:communities/flatten-channels-and-categories community-id])
        categories-indexes   (keep-indexed (fn [idx {:keys [render-as]}]
                                             (when (= render-as :category) idx))
                                           flatten-channels)
        channels-height      (->> flatten-channels
                                  (map (comp channel-component-heights :render-as))
                                  (reduce +))
        listing-height       (calc-listing-height)]
    (rn/use-effect
     (fn []
       (let [max-scroll-offset (calc-scrollable-content channels-height)]
         (if (neg? max-scroll-offset)
           (set-max-scroll 0)
           (set-max-scroll max-scroll-offset))))
     [channels-height])
    [rn/delay-render {:ms 120}
     [reanimated/flat-list
      {:ref                     flat-list-ref
       :style                   [(style/channel-listing theme listing-height)
                                 channels-styles]
       :data                    flatten-channels
       :content-container-style [(when platform/ios? {:padding-bottom safe-area/bottom})]
       :sticky-header-indices   categories-indexes
       :scroll-enabled          false
       :render-fn               render-fn
       :key-fn                  :id}]]))

(defn- header-cover-image
  [{:keys [cover-image background-color header-opacity]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/header-cover-image background-color)}
     [reanimated/image {:style style/cover-image :source {:uri cover-image}}]
     [rn/delay-render
      [reanimated/view {:style (style/cover-image-blur-container header-opacity)}
       [rn/image
        {:style       style/cover-image
         :source      {:uri cover-image}
         :blur-radius 20}]
       [rn/view {:style (style/cover-image-blur-layer theme)}]]]]))

(defn- open-community-options
  [community-id]
  (fn []
    (rf/dispatch
     [:show-bottom-sheet
      {:content (fn [] [options/community-options-bottom-sheet community-id])}])))

(defn- page-nav
  [{:keys [blur-version? nav-content-opacity community-name community-logo community-id]}]
  (let [on-options-press (rn/use-callback (open-community-options community-id))
        base-props       {:right-side          [{:icon-name           :i/options
                                                 :accessibility-label :community-options-for-community
                                                 :on-press            on-options-press}]
                          :icon-name           :i/close
                          :on-press            events.helper/navigate-back
                          :accessibility-label :back-button}]
    (if blur-version?
      [quo/page-nav
       (assoc base-props
              :type           :community
              :background     :blur
              :center-opacity nav-content-opacity
              :community-name community-name
              :community-logo community-logo)]
      [quo/page-nav
       (assoc base-props
              :type       :no-title
              :background :photo)])))

(defn- header
  [{:keys [community-id scroll-amount collapse-threshold sheet-displacement-threshold
           expand-header-limit]}]
  (let [header-opacity          (worklets/interpolate-value
                                 {:shared-value scroll-amount
                                  :input-range  [collapse-threshold sheet-displacement-threshold]
                                  :output-range [0 1]})
        opposite-header-opacity (worklets/use-opposite-header-opacity header-opacity)
        nav-content-opacity     (worklets/use-nav-content-opacity
                                 {:scroll-amount                   scroll-amount
                                  :sheet-displacement-threshold    sheet-displacement-threshold
                                  :navbar-content-threshold-factor navbar-content-threshold-factor
                                  :expand-header-limit             expand-header-limit})
        {:keys [community-name color logo
                cover-image]}   (rf/sub [:communities/community-overview community-id])]
    [:<>
     [header-cover-image
      {:cover-image      cover-image
       :background-color color
       :header-opacity   header-opacity}]
     [rn/delay-render
      [:<>
       [reanimated/view {:style (style/page-nav-container opposite-header-opacity)}
        [page-nav
         {:blur-version? false
          :community-id  community-id}]]
       [reanimated/view {:style (style/page-nav-container-blur header-opacity)}
        [page-nav
         {:blur-version?       true
          :community-id        community-id
          :nav-content-opacity nav-content-opacity
          :community-name      community-name
          :community-logo      logo}]]]]]))

(defn- community-logo
  [{:keys [initial-state scroll-amount community-id collapse-threshold
           sheet-displacement-threshold text-movement-threshold]}]
  (let [theme          (quo.context/use-theme)
        {:keys [logo]} (rf/sub [:communities/community-overview community-id])
        logo-styles    (worklets/use-logo-styles
                        {:initial-state                initial-state
                         :scroll-amount                scroll-amount
                         :collapse-threshold           collapse-threshold
                         :sheet-displacement-threshold sheet-displacement-threshold
                         :text-movement-threshold      text-movement-threshold})]
    [reanimated/view {:style [style/community-logo (style/community-logo-bg-color theme) logo-styles]}
     [rn/image {:style style/community-logo-image :source logo}]]))

(defn- name-and-description
  [{:keys [scroll-amount community-name community-description info-styles
           collapse-threshold text-movement-threshold initial-state]}]
  (let [name-styles (worklets/use-name-styles
                     {:initial-state           initial-state
                      :scroll-amount           scroll-amount
                      :collapse-threshold      collapse-threshold
                      :text-movement-threshold text-movement-threshold})]
    [rn/view {:style style/community-name-and-description}
     [reanimated/view {:style name-styles}
      [quo/text
       {:accessibility-label :community-title
        :weight              :semi-bold
        :size                :heading-1
        :number-of-lines     1}
       community-name]]
     [reanimated/view {:style [{:opacity 0} info-styles]}
      [quo/text
       {:accessibility-label :community-description
        :weight              :regular
        :size                :paragraph-1}
       community-description]]]))

(defn- community-info-tags
  [community-tags info-styles]
  (when (seq community-tags)
    [reanimated/view {:style info-styles}
     [quo/community-tags
      {:tags            community-tags
       :last-item-style style/community-tags-last-item
       :container-style style/community-tags}]]))

(defn- community-info-stats
  [{:keys [members-count active-members-count info-styles]}]
  [reanimated/view {:style [style/community-stats info-styles]}
   [rn/view {:style {:flex-direction :row :column-gap 12}}
    [quo/community-stat
     {:accessibility-label :stats-members-count
      :icon                :i/group
      :value               members-count}]
    [quo/community-stat
     {:accessibility-label :stats-active-count
      :icon                :i/active-members
      :value               active-members-count}]]])

(defn- community-info
  [{:keys [initial-state scroll-amount header-height community-id collapse-threshold
           sheet-displacement-threshold text-movement-threshold]}]
  (let [{:keys [community-name description active-members-count tags role-permissions?
                permissions color owner?
                joined?]} (rf/sub [:communities/community-overview community-id])
        theme             (quo.context/use-theme)
        sheet-styles      (worklets/use-sheet-styles
                           {:initial-state                initial-state
                            :scroll-amount                scroll-amount
                            :collapse-threshold           collapse-threshold
                            :sheet-displacement-threshold sheet-displacement-threshold})
        info-styles       (worklets/use-info-styles
                           {:initial-state                 initial-state
                            :scroll-amount                 scroll-amount
                            :collapse-threshold            collapse-threshold
                            :info-opacity-threshold-factor info-opacity-threshold-factor})
        get-dimensions    (rn/use-callback
                           (fn [e]
                             (let [height (oops/oget e "nativeEvent.layout.height")]
                               (reanimated/set-shared-value header-height (or height 0))
                               (reanimated/set-shared-value collapse-threshold (or (- height 16.5) 0))
                               (reagent/next-tick #(reanimated/set-shared-value initial-state
                                                                                "finalized")))))

        members-count     (rf/sub [:communities/community-members-count community-id])]
    [reanimated/view
     {:style     [(style/community-info theme) sheet-styles]
      :on-layout get-dimensions}
     [rn/delay-render
      [status-tag
       {:community-id community-id
        :joined?      joined?
        :info-styles  info-styles}]]
     [name-and-description
      {:initial-state           initial-state
       :scroll-amount           scroll-amount
       :community-name          community-name
       :community-description   description
       :info-styles             info-styles
       :collapse-threshold      collapse-threshold
       :text-movement-threshold text-movement-threshold}]
     [community-info-stats
      {:members-count        members-count
       :active-members-count active-members-count
       :info-styles          info-styles}]
     [community-info-tags tags info-styles]
     (when owner?
       [promotional-info-box-for-owners {:theme theme :info-styles info-styles}])
     [join-community
      {:community-id      community-id
       :joined?           joined?
       :tags?             (seq tags)
       :permissions       permissions
       :role-permissions? role-permissions?
       :color             color}]]))

(defn- community-sheet
  [{:keys [collapsed? community-id scroll-amount set-max-scroll collapse-threshold
           sheet-displacement-threshold expand-header-limit]}]
  (let [header-height           (reanimated/use-shared-value 0)
        initial-state           (reanimated/use-shared-value (if collapsed? "collapsed" "expanded"))
        text-movement-threshold (worklets/use-derived-value-mul collapse-threshold 0.7)]
    [rn/view {:style style/community-sheet-position}
     [community-logo
      {:initial-state                initial-state
       :community-id                 community-id
       :scroll-amount                scroll-amount
       :collapse-threshold           collapse-threshold
       :sheet-displacement-threshold sheet-displacement-threshold
       :text-movement-threshold      text-movement-threshold}]
     [community-info
      {:initial-state                initial-state
       :scroll-amount                scroll-amount
       :header-height                header-height
       :community-id                 community-id
       :collapse-threshold           collapse-threshold
       :sheet-displacement-threshold sheet-displacement-threshold
       :text-movement-threshold      text-movement-threshold}]
     [channel-listing
      {:community-id                 community-id
       :scroll-amount                scroll-amount
       :header-height                header-height
       :set-max-scroll               set-max-scroll
       :collapse-threshold           collapse-threshold
       :sheet-displacement-threshold sheet-displacement-threshold
       :expand-header-limit          expand-header-limit}]]))

(defn- community-overview
  [community-id collapsed?]
  (let [collapse-threshold           (reanimated/use-shared-value 0)
        sheet-displacement-threshold (worklets/use-derived-value-add collapse-threshold 8)
        expand-header-limit          (worklets/use-derived-value-add sheet-displacement-threshold 56)
        max-scroll                   (reanimated/use-shared-value 0)
        set-max-scroll               (rn/use-callback
                                      (fn [max-scroll-amount]
                                        (reanimated/set-shared-value max-scroll max-scroll-amount)))
        scroll-start                 (worklets/use-start-scroll-value collapsed? collapse-threshold)
        scroll-amount                (worklets/use-scroll-value collapsed? collapse-threshold)
        on-pan-start                 (worklets/on-pan-start scroll-start scroll-amount)
        on-pan-update                (worklets/on-pan-update
                                      {:scroll-start        scroll-start
                                       :scroll-amount       scroll-amount
                                       :max-scroll          max-scroll
                                       :expand-header-limit expand-header-limit})
        on-pan-end                   (worklets/on-pan-end
                                      {:scroll-start                 scroll-start
                                       :scroll-amount                scroll-amount
                                       :max-scroll                   max-scroll
                                       :expand-header-limit          expand-header-limit
                                       :collapse-threshold           collapse-threshold
                                       :snap-header-threshold-factor snap-header-threshold-factor
                                       :animation-duration           300})
        pan-gesture                  (-> (gesture/gesture-pan)
                                         (gesture/on-start on-pan-start)
                                         (gesture/on-update on-pan-update)
                                         (gesture/on-end on-pan-end))]
    [gesture/gesture-detector {:gesture pan-gesture}
     [rn/view {:style {:flex 1}}
      [header
       {:community-id                 community-id
        :scroll-amount                scroll-amount
        :collapse-threshold           collapse-threshold
        :sheet-displacement-threshold sheet-displacement-threshold
        :expand-header-limit          expand-header-limit}]
      [rn/delay-render
       [community-sheet
        {:collapsed?                   collapsed?
         :community-id                 community-id
         :scroll-amount                scroll-amount
         :set-max-scroll               set-max-scroll
         :collapse-threshold           collapse-threshold
         :sheet-displacement-threshold sheet-displacement-threshold
         :expand-header-limit          expand-header-limit}]]]]))

(defn- community-fetching-placeholder
  [id]
  (let [theme     (quo.context/use-theme)
        fetching? (rf/sub [:communities/fetching-community id])]
    [rn/view
     {:style               (style/fetching-placeholder safe-area/top)
      :accessibility-label (if fetching?
                             :fetching-community-overview
                             :failed-to-fetch-community-overview)}
     [quo/page-nav
      {:title      (i18n/label :t/community-overview)
       :type       :title
       :text-align :left
       :icon-name  :i/close
       :on-press   events.helper/navigate-back}]
     [quo/empty-state
      {:image           (resources/get-themed-image :cat-in-box theme)
       :description     (when-not fetching? (i18n/label :t/here-is-a-cat-in-a-box-instead))
       :title           (if fetching?
                          (i18n/label :t/fetching-community)
                          (i18n/label :t/failed-to-fetch-community))
       :container-style {:flex 1 :justify-content :center}}]]))

(defn view
  [id]
  (let [community-id      (or id (quo.context/use-screen-params))
        {:keys [collapsed?]
         :as   community} (rf/sub [:communities/community-overview community-id])]
    [rn/view {:style style/community-overview-container}
     (if community
       [community-overview community-id collapsed?]
       [community-fetching-placeholder community-id])]))
