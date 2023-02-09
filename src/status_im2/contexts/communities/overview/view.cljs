(ns status-im2.contexts.communities.overview.view
  (:require [utils.i18n :as i18n]
            [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.contexts.communities.overview.style :as style]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [status-im2.contexts.communities.menus.request-to-join.view :as join-menu]
            [quo2.components.navigation.floating-shell-button :as floating-shell-button]
            [status-im2.contexts.communities.overview.utils :as utils]
            [utils.re-frame :as rf]))


(defn preview-user-list
  [user-list]
  [rn/view style/preview-user
   [quo/preview-list
    {:type      :user
     :list-size (count user-list)
     :size      24}
    user-list]
   [quo/text
    {:accessibility-label :communities-screen-title
     :style               {:margin-left 8}
     :size                :label}
    (utils/join-existing-users-string user-list)]])

(defn channel-token-gating-details
  [name token-gating emoji channel-color]
  [rn/view {:height 350 :margin-top 20}
   [quo/token-gating
    {:channel {:name                   name
               :community-color        channel-color
               :emoji                  emoji
               :emoji-background-color channel-color
               :on-enter-channel       (fn []
                                         (js/alert
                                          "Entered channel"
                                          "Wuhuu!! You successfully entered the channel :)"))
               :gates                  token-gating}}]])

(defn open-channel-token-gating-details
  [name token-gating emoji channel-color]
  (rf/dispatch
   [:bottom-sheet/show-sheet
    {:content
     (fn []
       [channel-token-gating-details name token-gating emoji channel-color])
     :content-height 210}]))

(defn layout-y
  [event]
  (oops/oget event "nativeEvent.layout.y"))

(defn add-category-height
  [categories-heights category height]
  (swap! categories-heights
    (fn []
      (sort-by :height
               (conj @categories-heights
                     {:height height
                      :label  category})))))

(defn channel-list-component
  [{:keys [on-categories-heights-changed
           on-first-channel-height-changed]}
   channels-list]
  (let [categories-heights (reagent/atom [])]
    [rn/view
     {:on-layout #(on-first-channel-height-changed (+ (if platform/ios?
                                                        0
                                                        38)
                                                      (int (Math/ceil (layout-y %)))))
      :style     {:margin-top 20 :flex 1}}
     (map-indexed
      (fn [index [category channels-for-category]]
        [rn/view
         {:flex      1
          :key       (str index category)
          :on-layout #(do
                        (add-category-height categories-heights category (int (layout-y %)))
                        (on-categories-heights-changed @categories-heights))}

         [quo/divider-label
          {:label            category
           :chevron-position :left}]
         [rn/view
          {:margin-left   8
           :margin-top    10
           :margin-bottom 8}
          (map (fn [channel]
                 [rn/view
                  {:key        (:id channel)
                   :margin-top 4}
                  [quo/channel-list-item channel]])
               channels-for-category)]])
      channels-list)]))

(defn request-to-join-text
  [is-open?]
  (if is-open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join-community)))

(defn join-community
  [{:keys [joined can-join? requested-to-join-at
           community-color permissions]
    :as   community}]
  (let [pending?      (pos? requested-to-join-at)
        is-open?      (not= constants/community-channel-access-on-request (:access permissions))
        node-offline? (and can-join? (not joined) (pos? requested-to-join-at))]
    [:<>
     (when-not (or joined pending?)
       [quo/button
        {:on-press                  #(rf/dispatch
                                      [:bottom-sheet/show-sheet
                                       {:content                   (fn [] [join-menu/request-to-join
                                                                           community])
                                        :bottom-safe-area-spacing? false
                                        :content-height            300}])
         :accessibility-label       :show-request-to-join-screen-button
         :override-background-color community-color
         :style                     style/join-button
         :before                    :i/communities}
        (request-to-join-text is-open?)])

     (when (and (not (or joined pending?)) (not (or is-open? node-offline?)))
       [quo/text
        {:size  :paragraph-2
         :style style/review-notice}
        (i18n/label :t/community-admins-will-review-your-request)])

     (when node-offline?
       [quo/information-box
        {:type  :informative
         :icon  :i/info
         :style {:margin-top 12}}
        (i18n/label :t/request-processed-after-node-online)])]))

(defn get-tag
  [joined]
  [quo/status-tag
   {:status {:type (if joined :positive :pending)}
    :label  (if joined
              (i18n/label :t/joined)
              (i18n/label :t/pending))}])

(defn community-token-gating-details
  [name thumbnail-image tokens]
  [rn/view {:height 200 :margin-top 20}
   [quo/token-gating
    {:community {:name                     name
                 :community-color          colors/primary-50
                 :community-avatar-img-src thumbnail-image
                 :gates                    tokens}}]])

(defn add-on-press-handler
  [community-id {:keys [name emoji id locked? token-gating] :or {locked? false} :as chat}]
  (merge
   chat
   (if (and locked? token-gating)
     {:on-press #(open-channel-token-gating-details
                  name
                  token-gating
                  emoji
                  (colors/custom-color :pink 50))}

     (when (and (not locked?) id)
       {:on-press (fn []
                    (rf/dispatch [:dismiss-keyboard])
                    (rf/dispatch [:chat/navigate-to-chat (str community-id id)])
                    (rf/dispatch [:search/home-filter-changed nil]))}))))

(defn add-on-press-handler-to-chats
  [community-id chats]
  (mapv (partial add-on-press-handler community-id) chats))

(defn add-on-press-handler-to-categorized-chats
  [community-id categorized-chats]
  (reduce-kv (fn [acc category chats]
               (assoc acc category (add-on-press-handler-to-chats community-id chats)))
             {}
             categorized-chats))

(defn community-header
  [name]
  [quo/text
   {:accessibility-label :chat-name-text
    :number-of-lines     1
    :ellipsize-mode      :tail
    :weight              :semi-bold
    :size                :heading-1}
   name])

(defn community-description
  [description]
  [quo/text
   {:accessibility-label :community-description-text
    :number-of-lines     2
    :ellipsize-mode      :tail
    :weight              :regular
    :size                :paragraph-1
    :style               {:margin-top 8 :margin-bottom 12}}
   description])

(defn community-content
  [{:keys [name description locked joined images
           status tokens tags requested-to-join-at id]
    :as   community}
   {:keys [on-categories-heights-changed
           on-first-channel-height-changed]}]
  (let [pending?          (pos? requested-to-join-at)
        thumbnail-image   (get-in images [:thumbnail])
        chats-by-category (rf/sub [:communities/categorized-channels id])
        users             (rf/sub [:communities/users id])]
    [rn/view
     [rn/view {:padding-horizontal 20}
      (when (and (not joined)
                 (not pending?)
                 (= status :gated))
        [rn/view
         {:position :absolute
          :top      8
          :right    8}
         [quo/permission-tag-container
          {:locked   locked
           :status   status
           :tokens   tokens
           :on-press #(rf/dispatch
                       [:bottom-sheet/show-sheet
                        {:content-height 210
                         :content
                         (fn []
                           [community-token-gating-details
                            name
                            thumbnail-image
                            tokens])}])}]])
      (when (or pending? joined)
        [rn/view
         {:position :absolute
          :top      12
          :right    12}
         [get-tag joined]])
      [rn/view {:margin-top 56}
       [community-header name]]
      [community-description description]
      [quo/community-stats-column :card-view]
      [rn/view {:margin-top 12}]
      [quo/community-tags tags]
      [preview-user-list users]
      [join-community community]]
     [channel-list-component
      {:on-categories-heights-changed   #(on-categories-heights-changed %)
       :on-first-channel-height-changed #(on-first-channel-height-changed %)}
      (add-on-press-handler-to-categorized-chats id chats-by-category)]]))

(defn sticky-category-header
  [_]
  (fn [{:keys [:enabled :label]}]
    (when enabled
      [rn/view
       {:style style/blur-channel-header}
       [quo/divider-label
        {:label            (:label label)
         :chevron-position :left}]])))

(defn page-nav-right-section-buttons
  [id]
  [{:icon             :i/options
    :background-color (scroll-page/icon-color)
    :on-press         #(rf/dispatch
                        [:bottom-sheet/show-sheet
                         {:content
                          (fn []
                            [options/community-options-bottom-sheet
                             id])}])}])

(defn community-card-page-view
  [{:keys [name images id]}]
  (let [categories-heights   (reagent/atom [])
        first-channel-height (reagent/atom 0)
        scroll-height        (reagent/atom 0)
        cover                {:uri (get-in images [:large :uri])}]
    (fn [community]
      [scroll-page/scroll-page
       {:cover-image                    cover
        :page-nav-right-section-buttons (page-nav-right-section-buttons id)
        :name                           name
        :on-scroll                      #(reset! scroll-height %)
        :navigate-back?                 true
        :background-color               (colors/theme-colors
                                         colors/white
                                         colors/neutral-90)
        :height                         (if platform/ios?
                                          (if (> @scroll-height @first-channel-height)
                                            134
                                            100)
                                          (if (> @scroll-height @first-channel-height)
                                            140
                                            106))}

       [sticky-category-header
        {:enabled (> @scroll-height @first-channel-height)
         :label   (last (filter (fn [{:keys [height]}]
                                  (>= @scroll-height (+ height @first-channel-height)))
                                @categories-heights))}]
       [community-content
        community
        {:on-categories-heights-changed   #(reset! categories-heights %)
         :on-first-channel-height-changed #(reset! first-channel-height %)}]])))

(defn overview
  []
  (let [id        (rf/sub [:get-screen-params :community-overview])
        community (rf/sub [:communities/community id])]
    [rn/view
     {:style style/community-overview-container}
     [community-card-page-view community]
     [floating-shell-button/floating-shell-button
      {:jump-to {:on-press #(rf/dispatch [:shell/navigate-to-jump-to])
                 :label    (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   41}]]))
