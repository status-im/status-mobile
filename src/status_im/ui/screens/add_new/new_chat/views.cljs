(ns status-im.ui.screens.add-new.new-chat.views
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.design-system.colors :as colors]
    [quo.platform :as platform]
    [quo.react-native :as rn]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.ethereum.ens :as ens]
    [utils.i18n :as i18n]
    [status-im.multiaccounts.core :as multiaccounts]
    [status-im.qr-scanner.core :as qr-scanner]
    [status-im.ui.components.animation :as animation]
    [status-im.ui.components.chat-icon.screen :as chat-icon]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.invite.views :as invite]
    [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
    [status-im.ui.components.list.views :as list]
    [status-im.ui.components.react :as react]
    [status-im.ui.components.topbar :as topbar]
    [status-im.ui.screens.chat.photos :as photos]
    [status-im2.utils.validators :as validators]
    [status-im.utils.gfycat.core :as gfycat]
    [status-im.utils.identicon :as identicon]
    [status-im.utils.utils :as utils]
    [utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :as views]))

(defn- render-row
  [row]
  (let [first-name (first (multiaccounts/contact-two-names row false))]
    [quo/list-item
     {:title    first-name
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo row)]
      :on-press #(re-frame/dispatch [:chat.ui/start-chat
                                     (:public-key row)])}]))

(defn- icon-wrapper
  [color icon]
  [react/view
   {:style {:width            32
            :height           32
            :border-radius    25
            :align-items      :center
            :justify-content  :center
            :background-color color}}
   icon])

(defn- input-icon
  [state new-contact? entered-nickname blocked?]
  (let [icon (if new-contact? :main-icons/add :main-icons/arrow-right)]
    (cond
      (= state :searching)
      [icon-wrapper colors/gray
       [react/activity-indicator {:color colors/white-persist}]]

      (and (= state :valid) (not blocked?))
      [react/touchable-highlight
       {:on-press #(debounce/dispatch-and-chill [:contact.ui/contact-code-submitted new-contact?
                                                 entered-nickname]
                                                3000)}
       [icon-wrapper colors/blue
        [icons/icon icon {:color colors/white-persist}]]]

      :else
      [icon-wrapper colors/gray
       [icons/icon icon {:color colors/white-persist}]])))

(defn get-validation-label
  [value]
  (case value
    :invalid
    (i18n/label :t/profile-not-found)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(defn search-contacts
  [filter-text {:keys [name alias nickname]}]
  (or
   (string/includes? (string/lower-case (str name)) filter-text)
   (string/includes? (string/lower-case (str alias)) filter-text)
   (when nickname
     (string/includes? (string/lower-case (str nickname)) filter-text))))

(defn filter-contacts
  [filter-text contacts]
  (let [lower-filter-text (string/lower-case filter-text)]
    (if filter-text
      (filter (partial search-contacts lower-filter-text) contacts)
      contacts)))

(defn is-public-key?
  [k]
  (and
   (string? k)
   (string/starts-with? k "0x")))

(defn is-valid-username?
  [username]
  (let [is-chat-key? (and (is-public-key? username)
                          (= (count username) 132))
        is-ens?      (ens/valid-eth-name-prefix? username)]
    (or is-chat-key? is-ens?)))

(defn translate-anim
  [translate-y-value translate-y-anim-value]
  (animation/start
   (animation/timing translate-y-anim-value
                     {:toValue         translate-y-value
                      :duration        200
                      :useNativeDriver true})))
(views/defview new-chat
  []
  (views/letsubs [contacts                                  [:contacts/active]
                  {:keys [state ens-name public-key error]} [:contacts/new-identity]
                  search-value                              (reagent/atom "")
                  account                                   @(re-frame/subscribe [:multiaccount])
                  on-share                                  #(re-frame/dispatch
                                                              [:show-popover
                                                               {:view     :share-chat-key
                                                                :address  (account :public-key)
                                                                :ens-name (account :preferred-name)}])
                  my-profile-button-anim-y                  (animation/create-value 0)
                  keyboard-show-listener                    (atom nil)
                  keyboard-hide-listener                    (atom nil)
                  on-keyboard-show                          (fn []
                                                              ;; 42 is the bottom position so we
                                                              ;; translate it by 32 pts to leave 10 as
                                                              ;; margin
                                                              (translate-anim 32
                                                                              my-profile-button-anim-y))
                  on-keyboard-hide                          (fn []
                                                              (translate-anim 0
                                                                              my-profile-button-anim-y))
                  keyboard-show-event                       (if platform/android?
                                                              "keyboardDidShow"
                                                              "keyboardWillShow")
                  keyboard-hide-event                       (if platform/android?
                                                              "keyboardDidHide"
                                                              "keyboardWillHide")]
    {:component-did-mount
     (fn [_]
       (reset! keyboard-show-listener (.addListener react/keyboard keyboard-show-event on-keyboard-show))
       (reset! keyboard-hide-listener (.addListener react/keyboard
                                                    keyboard-hide-event
                                                    on-keyboard-hide)))
     :component-will-unmount
     (fn []
       (some-> ^js @keyboard-show-listener
               .remove)
       (some-> ^js @keyboard-hide-listener
               .remove))}
    [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
     [react/view {:style {:flex 1}}
      [topbar/topbar
       {:title (i18n/label :t/new-chat)
        :modal? true
        :right-accessories
        [{:icon                :qr
          :accessibility-label :scan-contact-code-button
          :on-press            #(re-frame/dispatch [::qr-scanner/scan-code
                                                    {:title   (i18n/label :t/new-chat)
                                                     :handler :contact/qr-code-scanned}])}]}]
      [react/view
       {:flex-direction :row
        :padding        16}
       [react/view {:flex 1}
        [quo/text-input
         {:on-change-text
          #(do
             (reset! search-value %)
             (re-frame/dispatch [:set-in [:contacts/new-identity :state] :empty])
             (debounce/debounce-and-dispatch [:contacts/set-new-identity %] 600))
          :on-submit-editing
          #(when (= state :valid)
             (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted false nil] 3000))
          :placeholder (i18n/label :t/enter-contact-code)
          :show-cancel false
          :accessibility-label :enter-contact-code-input
          :auto-capitalize :none
          :return-key-type :go
          :monospace true
          :auto-correct false}]]]
      [react/scroll-view
       {:style                        {:flex 1}
        :keyboard-dismiss-mode        :on-drag
        :keyboard-should-persist-taps :handled}
       [react/view
        (when (and
               (= (count contacts) 0)
               (= @search-value ""))
          {:flex 1})
        (if (and
             (= (count contacts) 0)
             (= @search-value ""))
          [react/view
           {:flex               1
            :align-items        :center
            :padding-horizontal 58
            :padding-top        160}
           [quo/text
            {:size  :base
             :align :center
             :color :secondary}
            (i18n/label :t/you-dont-have-contacts-invite-friends)]
           [invite/button]]
          [list/flat-list
           {:data      (filter-contacts @search-value contacts)
            :key-fn    :address
            :render-fn render-row}])]
       (when-not (= @search-value "")
         [react/view
          [quo/text
           {:style {:margin-horizontal 16
                    :margin-vertical   14}
            :size  :base
            :align :left
            :color :secondary}
           (i18n/label :t/non-contacts)]
          (when (and (= state :searching)
                     (is-valid-username? @search-value))
            [rn/activity-indicator
             {:color colors/gray
              :size  (if platform/android? :large :small)}])
          (if (= state :valid)
            [quo/list-item
             (merge
              {:title    (or ens-name (gfycat/generate-gfy public-key))
               :subtitle (if ens-name
                           (gfycat/generate-gfy public-key)
                           (utils/get-shortened-address public-key))
               :icon     [chat-icon/contact-icon-contacts-tab
                          (identicon/identicon public-key)]
               :on-press #(do
                            (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted false] 3000)
                            (re-frame/dispatch [:search/home-filter-changed nil]))}
              (when ens-name {:subtitle-secondary public-key}))]
            [quo/text
             {:style {:margin-horizontal 16}
              :size  :base
              :align :center
              :color :secondary}
             (if (is-valid-username? @search-value)
               (when (= state :error)
                 (get-validation-label error))
               (i18n/label :t/invalid-username-or-key))])])]
      (when-not (and
                 (= (count contacts) 0)
                 (= @search-value ""))
        [react/animated-view
         {:style {:height     36
                  :width      124
                  :position   :absolute
                  :bottom     42
                  :transform  [{:translateY my-profile-button-anim-y}]
                  :align-self :center}}
         [react/touchable-opacity
          {:style    {:padding-horizontal 2
                      :height             36
                      :width              124
                      :background-color   colors/blue
                      :border-radius      18
                      :elevation          4
                      :shadow-offset      {:width 0 :height 4}
                      :shadow-color       "rgba(0, 34, 51, 0.16)"
                      :shadow-radius      4
                      :shadow-opacity     1}
           :on-press on-share}
          [react/view
           {:style {:flex           1
                    :flex-direction :row
                    :align-items    :center}}
           [photos/photo
            (multiaccounts/displayed-photo account)
            {:size                32
             :accessibility-label :current-account-photo}]
           [quo/text
            {:size   :base
             :weight :medium
             :color  :inverse
             :style  {:margin-left 6}}
            (i18n/label :t/my-profile)]]]])]]))

(defn- nickname-input
  [entered-nickname]
  [quo/text-input
   {:on-change-text      #(reset! entered-nickname %)
    :auto-capitalize     :none
    :max-length          32
    :auto-focus          false
    :accessibility-label :nickname-input
    :placeholder         (i18n/label :t/add-nickname)
    :return-key-type     :done
    :auto-correct        false}])

(defn new-contact
  []
  (let [entered-nickname (reagent/atom "")]
    (fn []
      (let [{:keys [state ens-name public-key error]} @(re-frame/subscribe [:contacts/new-identity])
            blocked?                                  (and
                                                       (validators/valid-public-key? (or public-key ""))
                                                       @(re-frame/subscribe [:contacts/contact-blocked?
                                                                             public-key]))]
        [react/view {:style {:flex 1}}
         [topbar/topbar
          {:title (i18n/label :t/new-contact)
           :modal? true
           :right-accessories
           [{:icon                :qr
             :accessibility-label :scan-contact-code-button
             :on-press            #(re-frame/dispatch [::qr-scanner/scan-code
                                                       {:title        (i18n/label :t/new-contact)
                                                        :handler      :contact/qr-code-scanned
                                                        :new-contact? true
                                                        :nickname     @entered-nickname}])}]}]
         [react/view
          {:flex-direction :row
           :padding        16}
          [react/view
           {:flex          1
            :padding-right 16}
           [quo/text-input
            {:on-change-text
             #(do
                (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
                (debounce/debounce-and-dispatch [:contacts/set-new-identity %] 600))
             :on-submit-editing
             #(when (= state :valid)
                (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted true @entered-nickname]
                                             3000))
             :placeholder (i18n/label :t/enter-contact-code)
             :show-cancel false
             :accessibility-label :enter-contact-code-input
             :auto-capitalize :none
             :return-key-type :go}]]
          [react/view
           {:justify-content :center
            :align-items     :center}
           [input-icon state true @entered-nickname blocked?]]]
         [react/view {:min-height 30 :justify-content :flex-end :margin-bottom 16}
          [quo/text
           {:style {:margin-horizontal 16}
            :size  :small
            :align :center
            :color :secondary}
           (cond (= state :error)
                 (get-validation-label error)
                 (= state :valid)
                 (str (when ens-name (str ens-name " • "))
                      (utils/get-shortened-address public-key))
                 :else "")]]
         [react/text {:style {:margin-horizontal 16 :color colors/gray}}
          (i18n/label :t/nickname-description)]
         [react/view {:padding 16}
          [nickname-input entered-nickname]
          [react/text
           {:style {:align-self :flex-end
                    :margin-top 16
                    :color      colors/gray}}
           (str (count @entered-nickname) " / 32")]]]))))
