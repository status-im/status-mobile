(ns status-im.ui.screens.communities.reorder-categories
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [quo2.components.community.style :as styles]
            [reagent.core :as reagent]
            [status-im.communities.core :as communities]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.tabs :as tabs]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.communities.community :as community]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]))

(def data (reagent/atom []))
(def state (reagent/atom {:tab :chats}))

(defn show-delete-chat-confirmation
  [community-id chat-id]
  (utils/show-confirmation
   {:title               (i18n/label :t/delete-confirmation)
    :content             (i18n/label :t/delete-chat-confirmation)
    :confirm-button-text (i18n/label :t/delete)
    :on-accept           #(rf/dispatch [:delete-community-chat community-id chat-id])}))

(defn show-delete-category-confirmation
  [community-id category-id]
  (utils/show-confirmation
   {:title               (i18n/label :t/delete-confirmation)
    :content             (i18n/label :t/delete-category-confirmation)
    :confirm-button-text (i18n/label :t/delete)
    :on-accept           #(rf/dispatch [:delete-community-category community-id category-id])}))

(defn categories-tab?
  []
  (let [{:keys [tab]} @state]
    (= tab :categories)))

(defn chat-item
  [{:keys [id community-id] :as home-item} is-active? drag]
  (let [chat-id          (string/replace id community-id "")
        background-color (if is-active? colors/gray-lighter colors/white)
        home-item        (clojure.set/rename-keys home-item {:id :chat-id})]
    [rn/view
     {:accessibility-label :chat-item
      :style               (merge styles/category-item
                                  {:background-color background-color})}
     [rn/touchable-opacity
      {:accessibility-label :delete-community-chat
       :on-press            #(show-delete-chat-confirmation community-id chat-id)}
      [icons/icon :main-icons/delete-circle {:no-color true}]]
     [rn/view {:flex 1}
      [inner-item/home-list-item-old (assoc home-item :edit? true) {:active-opacity 1}]]
     [rn/touchable-opacity
      {:on-long-press       drag
       :delay-long-press    100
       :accessibility-label :chat-drag-handle
       :style               {:padding 20}}
      [icons/icon :main-icons/reorder-handle {:no-color true :width 18 :height 12}]]]))

(defn category-item
  [{:keys [id name community-id]} is-active? drag]
  (let [background-color (if is-active? colors/gray-lighter colors/white)
        category-none?   (string/blank? id)]
    [:<>
     [quo/separator]
     [rn/view
      {:accessibility-label :category-item
       :style               (merge styles/category-item
                                   {:background-color background-color})}
      (if (not (categories-tab?))
        [icons/icon :main-icons/channel-category {:color colors/gray}]
        [rn/touchable-opacity
         {:accessibility-label :delete-category-button
          :on-press            #(show-delete-category-confirmation community-id id)}
         [icons/icon :main-icons/delete-circle {:no-color true}]])
      [rn/view {:flex 1}
       [rn/text {:style {:font-size 17 :margin-left 10 :color colors/black}} name]]
      (when (and (not category-none?) (categories-tab?))
        [rn/touchable-opacity
         {:accessibility-label :category-drag-handle
          :on-long-press       drag
          :delay-long-press    100
          :style               {:padding 20}}
         [icons/icon :main-icons/reorder-handle {:no-color true :width 18 :height 12}]])]]))

(defn render-fn
  [{:keys [chat-type] :as item} _ _ _ is-active? drag]
  (if (= chat-type constants/community-chat-type)
    [chat-item item is-active? drag]
    [category-item item is-active? drag]))

(defn calculate-chat-new-position-and-category
  [to second-call? old-category going-up?]
  (let [{:keys [id chat-type categoryID position]} (get @data to)
        [new-category new-position]
        (if going-up?
          (if (= chat-type constants/community-chat-type)
            [categoryID (if second-call? (inc position) position)]
            (if second-call?
              [id 0]
              (calculate-chat-new-position-and-category (dec to) true old-category true)))
          (if (= chat-type constants/community-chat-type)
            (if (= categoryID old-category)
              [categoryID position]
              [categoryID (inc position)])
            [id 0]))]
    [new-category new-position]))

(defn update-local-atom
  [data-js]
  (reset! data data-js)
  (reagent/flush))

(defn on-drag-end-chat
  [from to data-js]
  (let [{:keys [id community-id categoryID position]} (get @data from)
        [new-category new-position]                   (calculate-chat-new-position-and-category
                                                       to
                                                       false
                                                       categoryID
                                                       (> from to))
        chat-id                                       (string/replace id community-id "")]
    (when-not (and (= new-position position) (= new-category categoryID))
      (update-local-atom data-js)
      (rf/dispatch [::communities/reorder-community-category-chat
                    community-id new-category chat-id new-position]))))

(defn on-drag-end-category
  [from to data-js]
  (let [{:keys [id community-id position]} (get @data from)]
    (when (and (< to (count @data)) (not= position to) (not= id ""))
      (update-local-atom data-js)
      (rf/dispatch [::communities/reorder-community-category community-id id to]))))

(defn on-drag-end-fn
  [from to data-js]
  (if (categories-tab?)
    (on-drag-end-category from to data-js)
    (when-not (= to 0) (on-drag-end-chat from to data-js))))

(defn reset-data
  [categories chats]
  (reset! data
    (if (categories-tab?)
      categories
      (walk/postwalk-replace
       {:chat-id :id}
       (reduce (fn [acc category]
                 (-> acc
                     (conj category)
                     (into (get chats (:id category)))))
               []
               categories)))))

(defn draggable-list
  []
  [rn/draggable-flat-list
   {:key-fn               :id
    :data                 @data
    :render-fn            render-fn
    :autoscroll-threshold (if platform/android? 150 250)
    :autoscroll-speed     (if platform/android? 10 150) ;; TODO - Use same speed for both ios and android
    :container-style      {:margin-bottom 108}          ;;        after bumping react native version to >
                                                        ;;        0.64
    :on-drag-end-fn       on-drag-end-fn}])

(defn chats_and_categories
  []
  (let [{:keys [tab]} @state]
    [:<>
     [react/view
      {:flex-direction    :row
       :margin-horizontal 16
       :margin-vertical   8
       :border-radius     8
       :background-color  colors/blue-light}
      [tabs/tab-button state :chats (i18n/label :t/edit-chats) (= tab :chats)]
      [tabs/tab-button state :categories (i18n/label :t/edit-categories) (= tab :categories)]]]))

(defn view
  []
  (let [{:keys [community-id]}                             (rf/sub [:get-screen-params])
        {:keys [id name images members permissions color]}
        (rf/sub [:communities/community community-id])
        sorted-categories                                  (rf/sub [:communities/sorted-categories
                                                                    community-id])
        categories                                         (if (categories-tab?)
                                                             sorted-categories
                                                             (conj sorted-categories
                                                                   {:id           ""
                                                                    :position     (count
                                                                                   sorted-categories)
                                                                    :name         (i18n/label :t/none)
                                                                    :community-id community-id}))
        chats                                              (rf/sub
                                                            [:chats/sorted-categories-by-community-id
                                                             community-id])]
    (reset-data categories chats)
    [:<>
     [topbar/topbar
      {:content [community/toolbar-content id name color images
                 (not= (:access permissions) constants/community-no-membership-access)
                 (count members)]}]
     (if (and (empty? sorted-categories) (empty? chats))
       [community/blank-page (i18n/label :t/welcome-community-blank-message-edit-chats)]
       [:<>
        [chats_and_categories]
        [draggable-list]])]))
