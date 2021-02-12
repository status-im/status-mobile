(ns status-im.ui.screens.intro.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.create.core :refer [step-kw-to-num]]
            [status-im.privacy-policy.core :as privacy-policy]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.intro.styles :as styles]
            [status-im.utils.config :as config]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.debounce :refer [dispatch-and-chill]]
            [quo.core :as quo]
            [status-im.ui.screens.intro.carousel :as carousel]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview intro []
  (letsubs  [{window-height :height} [:dimensions/window]
             view-id [:view-id]]
    [react/view {:style styles/intro-view}
     [carousel/viewer [{:image (resources/get-theme-image :chat)
                        :title :intro-title1
                        :text :intro-text1}
                       {:image (resources/get-theme-image :wallet)
                        :title :intro-title2
                        :text :intro-text2}
                       {:image (resources/get-theme-image :browser)
                        :title :intro-title3
                        :text :intro-text3}] window-height view-id]
     [react/view styles/buttons-container
      [react/view {:style (assoc styles/bottom-button :margin-bottom 16)}
       [quo/button {:on-press #(re-frame/dispatch [:multiaccounts.create.ui/intro-wizard])}
        (i18n/label :t/get-started)]]
      [react/nested-text
       {:style styles/welcome-text-bottom-note}
       (i18n/label :t/intro-privacy-policy-note1)
       [{:style    (assoc styles/welcome-text-bottom-note :color colors/blue)
         :on-press privacy-policy/open-privacy-policy-link!}
        (i18n/label :t/intro-privacy-policy-note2)]]]]))

(defn generate-key []
  (let [dimensions (r/atom {})]
    (fn []
      [react/view {:on-layout  (fn [^js e]
                                 (reset! dimensions (js->clj (-> e .-nativeEvent .-layout) :keywordize-keys true)))
                   :style {:align-items :center
                           :justify-content :center
                           :flex 1}}
       (let [padding    40
             image-size (- (min (:width @dimensions) (:height @dimensions)) padding)]
         [react/image {:source (resources/get-theme-image :keys)
                       :resize-mode :contain
                       :style {:width image-size :height image-size}}])])))

(defn choose-key [{:keys [multiaccounts selected-id]}]
  [react/view
   {:style {:flex            1
            :justify-content :center}}
   [react/scroll-view
    {:style                   {:max-height 410}
     :content-container-style {:justify-content :flex-start}}
    (for [[acc accessibility-n] (map vector multiaccounts (range (count multiaccounts)))]
      (let [selected?  (= (:id acc) selected-id)
            public-key (get-in acc [:derived constants/path-whisper-keyword :public-key])]
        ^{:key public-key}
        [quo/list-item {:accessibility-label (keyword (str "select-account-button-" accessibility-n))
                        :active              selected?
                        :title               [quo/text {:number-of-lines     2
                                                        :weight              :medium
                                                        :ellipsize-mode      :middle
                                                        :accessibility-label :username}
                                              (gfy/generate-gfy public-key)]
                        :subtitle            [quo/text {:weight :monospace
                                                        :color  :secondary}
                                              (utils/get-shortened-address public-key)]
                        :accessory           :radio
                        :on-press            #(re-frame/dispatch [:intro-wizard/on-key-selected (:id acc)])
                        :icon                [react/image {:source      {:uri (identicon/identicon public-key)}
                                                           :resize-mode :cover
                                                           :style       styles/multiaccount-image}]}]))]])

(defn storage-entry [{:keys [type icon icon-width icon-height
                             image image-selected image-width image-height
                             title desc]} selected-storage-type]
  (let [selected? (= type selected-storage-type)]
    [react/view
     {:style {:flex 1
              :padding-top 14}}
     [react/view {:style {:padding-bottom 4}}
      [react/text {:style (assoc styles/wizard-text :text-align :left :margin-left 16)}
       (i18n/label type)]]
     [react/touchable-highlight
      {:accessibility-label (keyword (str "select-storage-" type))
       :on-press #(re-frame/dispatch
                   [:intro-wizard/on-key-storage-selected
                    (if (or platform/android?
                            config/keycard-test-menu-enabled?)
                      type
                      :default)])}
      [react/view (assoc (styles/list-item selected?)
                         :align-items :flex-start
                         :padding-top 16
                         :padding-bottom 12)
       (if image
         [react/image
          {:source (resources/get-image (if selected? image-selected image))
           :style  {:width image-width :height image-height}}]
         [icons/icon icon {:color (if selected? colors/blue colors/gray)
                           :width icon-width :height icon-height}])
       [react/view {:style {:margin-horizontal 16 :flex 1}}
        [react/text {:style (assoc styles/wizard-text :font-weight "500" :color colors/black :text-align :left)}
         (i18n/label title)]
        [react/view {:style {:min-height 4 :max-height 4}}]
        [react/text {:style (assoc styles/wizard-text :text-align :left)}
         (i18n/label desc)]]
       [radio/radio selected?]]]]))

(defn select-key-storage [{:keys [selected-storage-type]}]
  (let [storage-types [{:type        :default
                        :icon        :main-icons/mobile
                        :icon-width  24
                        :icon-height 24
                        :title       :this-device
                        :desc        :this-device-desc}
                       {:type           :advanced
                        :image          :keycard-logo-gray
                        :image-selected :keycard-logo-blue
                        :image-width    24
                        :image-height   24
                        :title          :keycard
                        :desc           :keycard-desc}]]
    [react/view
     {:style {:flex            1
              :justify-content :center}}
     [react/view
      {:style
       {:max-height       420
        :flex             1
        :justify-content  :flex-start}}
      [react/view {:style {:justify-content :flex-start
                           :height          264}}
       [storage-entry (first storage-types) selected-storage-type]
       [react/view {:style {:flex       1
                            :max-height 16}}]
       [storage-entry (second storage-types) selected-storage-type]]]]))

(defn bottom-bar [{:keys [step weak-password?
                          forward-action
                          next-button-disabled?
                          processing? existing-account?]}]
  [react/view {:style {:align-items :center}}
   (cond (and (#{:generate-key :recovery-success} step) processing?)
         [react/view {:min-height 46 :max-height 46 :align-self :stretch :margin-bottom 16}
          [react/activity-indicator {:animating true
                                     :size      :large}]]
         (#{:generate-key :recovery-success} step)
         (let [label-kw (case step
                          :generate-key     :t/generate-a-key
                          :recovery-success :t/re-encrypt-key
                          :intro-wizard-title6)]
           [react/view {:style (assoc styles/bottom-button :margin-bottom 16)}
            [quo/button
             {:disabled            existing-account?
              :on-press            #(re-frame/dispatch [forward-action])
              :accessibility-label :onboarding-next-button}
             (i18n/label label-kw)]])
         :else
         [toolbar/toolbar
          {:show-border? true
           :right        [quo/button
                          {:on-press            #(dispatch-and-chill [forward-action] 300)
                           :accessibility-label :onboarding-next-button
                           :disabled            (or processing?
                                                    (and (= step :create-code) weak-password?)
                                                    (and (= step :enter-phrase) next-button-disabled?))
                           :type                :secondary
                           :after               :main-icons/next}
                          (i18n/label :t/next)]}])
   (when (and (= :generate-key step) (not processing?))
     [react/view {:padding-vertical 8}
      [quo/button
       {:on-press #(re-frame/dispatch
                    [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
        :type     :secondary}
       (i18n/label :t/access-existing-keys)]])
   (when (or (= :generate-key step) (and processing? (= :recovery-success step)))
     [react/text {:style (assoc styles/wizard-text :margin-top 20 :margin-bottom 16)}
      (i18n/label (cond (= :recovery-success step)
                        :t/processing
                        processing? :t/generating-keys
                        :else       :t/this-will-take-few-seconds))])])

(defn top-bar [{:keys [step]}]
  (let [hide-subtitle? (or (= step :enter-phrase))]
    [react/view {:style {:margin-top   16
                         :margin-horizontal 32}}

     [react/text {:style (cond-> styles/wizard-title
                           hide-subtitle?
                           (assoc :margin-bottom 0))}
      (i18n/label
       (cond (= step :enter-phrase)
             :t/multiaccounts-recover-enter-phrase-title
             (= step :recovery-success)
             :t/keycard-recovery-success-header
             :else (keyword (str "intro-wizard-title" (step-kw-to-num step)))))]
     (cond (#{:choose-key :select-key-storage} step)
           ; Use nested text for the "Learn more" link
           [react/nested-text {:style (merge styles/wizard-text
                                             {:height 60})}
            (str (i18n/label (keyword (str "intro-wizard-text" (step-kw-to-num step)))) " ")
            [{:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet :learn-more
                                             {:title (i18n/label (if (= step :choose-key) :t/about-names-title :t/about-key-storage-title))
                                              :content  (i18n/label (if (= step :choose-key) :t/about-names-content :t/about-key-storage-content))}])
              :style {:color colors/blue}
              :accessibility-label :learn-more}
             (i18n/label :learn-more)]]
           (not hide-subtitle?)
           [react/text {:style styles/wizard-text}
            (i18n/label (cond (= step :recovery-success)
                              :t/recovery-success-text
                              :else (keyword (str "intro-wizard-text"
                                                  (step-kw-to-num step)))))]
           :else nil)]))

(defn enter-phrase [{:keys [processing?
                            passphrase-word-count
                            next-button-disabled?
                            passphrase-error]}]
  [react/keyboard-avoiding-view {:flex             1
                                 :background-color colors/white}
   [react/view {:background-color   colors/white
                :flex               1
                :justify-content    :center
                :padding-horizontal 16}
    [quo/text-input
     {:on-change-text      #(re-frame/dispatch [:multiaccounts.recover/enter-phrase-input-changed (security/mask-data %)])
      :auto-focus          true
      :error               (when passphrase-error (i18n/label passphrase-error))
      :accessibility-label :passphrase-input
      :placeholder         (i18n/label :t/seed-phrase-placeholder)
      :show-cancel         false
      :bottom-value        40
      :multiline           true
      :auto-correct        false
      :monospace           true}]
    [react/view {:align-items :flex-end}
     [react/view {:flex-direction   :row
                  :align-items      :center
                  :padding-vertical 8
                  :opacity          (if passphrase-word-count 1 0)}
      [quo/text {:color (if next-button-disabled? :secondary :main)
                 :size  :small}
       (when-not next-button-disabled?
         "✓ ")
       (i18n/label-pluralize passphrase-word-count :t/words-n)]]]]
   [react/view {:align-items :center}
    [react/text {:style {:color         colors/gray
                         :font-size     14
                         :margin-bottom 8
                         :text-align    :center}}
     (i18n/label :t/multiaccounts-recover-enter-phrase-text)]
    (when processing?
      [react/view {:flex 1 :align-items :center}
       [react/activity-indicator {:size      :large
                                  :animating true}]
       [react/text {:style {:color      colors/gray
                            :margin-top 8}}
        (i18n/label :t/processing)]])]])

(defn recovery-success [pubkey name photo-path]
  [react/view {:flex             1
               :justify-content  :space-between
               :background-color colors/white}
   [react/view {:flex            1
                :justify-content :space-between
                :align-items     :center}
    [react/view {:flex-direction  :column
                 :flex            1
                 :justify-content :center
                 :align-items     :center}
     [react/view {:margin-horizontal 16
                  :flex-direction    :column}
      [react/view {:justify-content :center
                   :align-items     :center
                   :margin-bottom   11}
       [react/image {:source {:uri photo-path}
                     :style  {:width         61
                              :height        61
                              :border-radius 30
                              :border-width  1
                              :border-color  colors/black-transparent}}]]
      [react/text {:style           {:text-align  :center
                                     :color       colors/black
                                     :font-weight "500"}
                   :number-of-lines 1
                   :ellipsize-mode  :middle}
       name]
      [quo/text {:style           {:margin-top 4}
                 :monospace       true
                 :color           :secondary
                 :align           :center
                 :number-of-lines 1
                 :ellipsize-mode  :middle}
       (utils/get-shortened-address pubkey)]]]]])

(defview wizard-generate-key []
  (letsubs [wizard-state [:intro-wizard/generate-key]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:border-bottom false
       :navigation
       {:on-press #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex            1
                          :justify-content :space-between}}
      [top-bar {:step :generate-key}]
      [generate-key]
      [bottom-bar {:step           :generate-key
                   :forward-action :intro-wizard/step-forward-pressed
                   :processing?    (:processing? wizard-state)}]]]))

(defview wizard-choose-key []
  (letsubs [wizard-state [:intro-wizard/choose-key]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:border-bottom false
       :navigation
       {:label    (i18n/label :t/cancel)
        :on-press #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :choose-key}]
      [choose-key wizard-state]
      [bottom-bar {:step :choose-key
                   :forward-action :intro-wizard/step-forward-pressed}]]]))

(defview wizard-select-key-storage []
  (letsubs [wizard-state [:intro-wizard/select-key-storage]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       (if (:recovering? wizard-state)
         {:label   (i18n/label :t/cancel)
          :on-press #(re-frame/dispatch [:intro-wizard/navigate-back])}
         {:on-press #(re-frame/dispatch [:intro-wizard/navigate-back])})}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :select-key-storage}]
      [select-key-storage wizard-state]
      [bottom-bar {:step :select-key-storage
                   :forward-action (:forward-action wizard-state)}]]]))

(defview wizard-enter-phrase []
  (letsubs [wizard-state [:intro-wizard/enter-phrase]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [topbar/topbar
      {:border-bottom false
       :navigation
       {:on-press #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex            1
                          :justify-content :space-between}}
      [top-bar {:step :enter-phrase}]
      [enter-phrase wizard-state]
      [bottom-bar (merge {:step :enter-phrase
                          :forward-action :multiaccounts.recover/enter-phrase-next-pressed}
                         wizard-state)]]]))

(defview wizard-recovery-success []
  (letsubs [{:keys [pubkey processing? name identicon]} [:intro-wizard/recovery-success]
            existing-account? [:intro-wizard/recover-existing-account?]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:border-bottom false
       :navigation
       {:on-press #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :recovery-success}]
      [recovery-success pubkey name identicon]
      [bottom-bar {:step              :recovery-success
                   :forward-action    :multiaccounts.recover/re-encrypt-pressed
                   :processing?       processing?
                   :existing-account? existing-account?}]]]))
