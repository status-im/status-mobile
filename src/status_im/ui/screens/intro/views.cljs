(ns status-im.ui.screens.intro.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.privacy-policy.core :as privacy-policy]
            [status-im.utils.utils :as utils]
            [status-im.multiaccounts.create.core :refer [step-kw-to-num]]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.identicon :as identicon]
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.text-input.view :as text-input]
            [taoensso.timbre :as log]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as r]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.screens.intro.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.topbar :as topbar]))

(defn dots-selector [{:keys [on-press n selected color]}]
  [react/view {:style (styles/dot-selector n)}
   (doall
    (for [i (range n)]
      ^{:key i}
      [react/view {:style (styles/dot color (selected i))}]))])

(defn intro-viewer [slides window-height]
  (let [scroll-x (r/atom 0)
        scroll-view-ref (atom nil)
        width (r/atom 0)
        height (r/atom 0)
        bottom-margin (if (> window-height 600) 32 16)]
    (fn []
      [react/view {:style {:align-items :center
                           :flex 1
                           :margin-bottom bottom-margin
                           :justify-content :flex-end}
                   :on-layout (fn [e]
                                (reset! width (-> e .-nativeEvent .-layout .-width)))}
       [react/scroll-view {:horizontal true
                           :paging-enabled true
                           :ref #(reset! scroll-view-ref %)
                           :shows-vertical-scroll-indicator false
                           :shows-horizontal-scroll-indicator false
                           :pinch-gesture-enabled false
                           :on-scroll #(let [x (.-nativeEvent.contentOffset.x %)]
                                         (reset! scroll-x x))
                           :style {;:width @width
                                   :margin-bottom bottom-margin}}
        (doall
         (for [s slides]
           ^{:key (:title s)}
           [react/view {:style {:flex 1
                                :width @width
                                :justify-content :flex-end
                                :align-items :center
                                :padding-horizontal 32}}
            (let [margin 32
                  size (min @width @height) #_(- (min @width @height) #_(* 2 margin))]
              [react/view {:style {:flex 1}
                           :on-layout (fn [e]
                                        (reset! height (-> e .-nativeEvent .-layout .-height)))}
               [react/image {:source (:image s)
                             :resize-mode :contain
                             :style {:width size
                                     :height size}}]])
            [react/i18n-text {:style styles/wizard-title :key (:title s)}]
            [react/i18n-text {:style styles/wizard-text
                              :key   (:text s)}]]))]
       (let [selected (hash-set (quot (int @scroll-x) (int @width)))]
         [dots-selector {:selected selected :n (count slides)
                         :color colors/blue}])])))

(defview intro []
  (letsubs  [{window-height :height} [:dimensions/window]]
    [react/view {:style styles/intro-view}
     [intro-viewer [{:image (resources/get-theme-image :chat)
                     :title :intro-title1
                     :text :intro-text1}
                    {:image (resources/get-theme-image :wallet)
                     :title :intro-title2
                     :text :intro-text2}
                    {:image (resources/get-theme-image :browser)
                     :title :intro-title3
                     :text :intro-text3}] window-height]
     [react/view styles/buttons-container
      [components.common/button {:button-style (assoc styles/bottom-button :margin-bottom 16)
                                 :on-press     #(re-frame/dispatch [:multiaccounts.create.ui/intro-wizard])
                                 :label        (i18n/label :t/get-started)}]
      [components.common/button {:button-style (assoc styles/bottom-button :margin-bottom 24)
                                 :on-press    #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                                 :label       (i18n/label :t/access-key)
                                 :background? false}]
      [react/nested-text
       {:style styles/welcome-text-bottom-note}
       (i18n/label :t/intro-privacy-policy-note1)
       [{:style (assoc styles/welcome-text-bottom-note :color colors/blue)
         :on-press privacy-policy/open-privacy-policy-link!}
        (i18n/label :t/intro-privacy-policy-note2)]]]]))

(defn generate-key []
  (let [dimensions (r/atom {})]
    (fn []
      [react/view {:on-layout  (fn [e]
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
        [react/touchable-highlight
         {:accessibility-label (keyword (str "select-account-button-" accessibility-n))
          :on-press            #(re-frame/dispatch [:intro-wizard/on-key-selected (:id acc)])}
         [react/view {:style (styles/list-item selected?)}
          [react/view {:style styles/list-item-body}
           [react/image {:source      {:uri (identicon/identicon public-key)}
                         :resize-mode :cover
                         :style       styles/multiaccount-image}]
           [react/view {:style {:padding-horizontal 16
                                :flex               1}}
            [react/text {:style           (assoc styles/wizard-text :text-align :left
                                                 :color colors/black
                                                 :line-height 22
                                                 :font-weight "500")
                         :number-of-lines 2
                         :ellipsize-mode  :middle}
             (gfy/generate-gfy public-key)]
            [react/text {:style (assoc styles/wizard-text
                                       :text-align :left
                                       :line-height 22
                                       :font-family "monospace")}
             (utils/get-shortened-address public-key)]]]
          [radio/radio selected?]]]))]])

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
       :on-press #(re-frame/dispatch [:intro-wizard/on-key-storage-selected (if (and config/hardwallet-enabled?
                                                                                     platform/android?) type :default)])}
      [react/view (assoc (styles/list-item selected?)
                         :align-items :flex-start
                         :padding-top 16
                         :padding-bottom 12)
       (if image
         [react/image
          {:source (resources/get-image (if selected? image-selected image))
           :style  {:width image-width :height image-height}}]
         [vector-icons/icon icon {:color (if selected? colors/blue colors/gray)
                                  :width icon-width :height icon-height}])
       [react/view {:style {:margin-horizontal 16 :flex 1}}
        [react/text {:style (assoc styles/wizard-text :font-weight "500" :color colors/black :text-align :left)}
         (i18n/label title)]
        [react/view {:style {:min-height 4 :max-height 4}}]
        [react/text {:style (assoc styles/wizard-text :text-align :left)}
         (i18n/label desc)]]
       [radio/radio selected?]]]]))

(defn select-key-storage [{:keys [selected-storage-type view-height]}]
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

(defn password-container [confirm-failure? view-width]
  (let [horizontal-margin 16]
    [react/view {:style {:flex 1
                         :justify-content :space-between
                         :align-items :center :margin-horizontal horizontal-margin}}
     [react/view {:style {:justify-content :center :flex 1}}
      [react/text {:style (assoc styles/wizard-text :color colors/red
                                 :margin-bottom 16)}
       (if confirm-failure? (i18n/label :t/password_error1) " ")]

      [react/text-input {:secure-text-entry true
                         :auto-capitalize :none
                         :auto-focus true
                         :accessibility-label :password-input
                         :text-align :center
                         :placeholder ""
                         :style (styles/password-text-input (- view-width (* 2 horizontal-margin)))
                         :on-change-text #(re-frame/dispatch [:intro-wizard/code-symbol-pressed %])}]]
     [react/text {:style (assoc styles/wizard-text :margin-bottom 16)} (i18n/label :t/password-description)]]))

(defn create-code [{:keys [confirm-failure? view-width]}]
  [password-container confirm-failure? view-width])

(defn confirm-code [{:keys [confirm-failure? processing? view-width]}]
  (if processing?
    [react/view {:style {:justify-content :center
                         :align-items :center}}
     [react/activity-indicator {:size      :large
                                :animating true}]
     [react/text {:style {:color      colors/gray
                          :margin-top 8}}
      (i18n/label :t/processing)]]
    [password-container confirm-failure? view-width]))

(defn bottom-bar [{:keys [step weak-password? encrypt-with-password?
                          forward-action
                          next-button-disabled?
                          processing? existing-account?] :as wizard-state}]
  [react/view {:style {:margin-bottom (if (or (#{:choose-key :select-key-storage
                                                 :enter-phrase :recovery-success} step)
                                              (and (#{:create-code :confirm-code} step)
                                                   encrypt-with-password?))
                                        20
                                        32)
                       :align-items :center}}
   (cond (and (#{:generate-key :recovery-success} step) processing?)
         [react/view {:min-height 46 :max-height 46 :align-self :stretch}
          [react/activity-indicator {:animating true
                                     :size      :large}]]
         (#{:generate-key :recovery-success} step)
         (let [label-kw (case step
                          :generate-key :generate-a-key
                          :recovery-success :re-encrypt-key
                          :intro-wizard-title6)]
           [react/view {:min-height 46 :max-height 46}
            [components.common/button
             {:button-style        (if existing-account?
                                     styles/disabled-bottom-button
                                     styles/bottom-button)
              :on-press            (when-not existing-account?
                                     #(re-frame/dispatch [forward-action]))
              :accessibility-label :onboarding-next-button
              :label               (i18n/label label-kw)
              :label-style         (when existing-account?
                                     styles/disabled-bottom-button-text)}]])
         (and (#{:create-code :confirm-code} step)
              (not encrypt-with-password?))
         [components.common/button {:button-style styles/bottom-button
                                    :label (i18n/label :t/encrypt-with-password)
                                    :accessibility-label :encrypt-with-password-button
                                    :on-press #(re-frame/dispatch [:intro-wizard/on-encrypt-with-password-pressed])
                                    :background? false}]

         :else
         [react/view {:style (styles/bottom-arrow)}
          [react/view {:style {:margin-right 10}}
           [components.common/bottom-button {:on-press  #(re-frame/dispatch [forward-action])
                                             :accessibility-label :onboarding-next-button
                                             :disabled? (or processing?
                                                            (and (= step :create-code) weak-password?)
                                                            (and (= step :enter-phrase) next-button-disabled?))
                                             :forward? true}]]])

   (when (or (= :generate-key step) (and processing? (= :recovery-success step)))
     [react/text {:style (assoc styles/wizard-text :margin-top 20)}
      (i18n/label (cond (= :recovery-success step)
                        :t/processing
                        processing? :t/generating-keys
                        :else :t/this-will-take-few-seconds))])])

(defn top-bar [{:keys [step encrypt-with-password?]}]
  (let [hide-subtitle? (or (= step :confirm-code)
                           (= step :enter-phrase)
                           (and (#{:create-code :confirm-code} step) encrypt-with-password?))]
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
             :else (keyword (str "intro-wizard-title"
                                 (when  (and (#{:create-code :confirm-code} step) encrypt-with-password?)
                                   "-alt") (step-kw-to-num step)))))]
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
                            passphrase-error] :as wizard-state}]
  [react/keyboard-avoiding-view {:flex             1
                                 :justify-content  :flex-start
                                 :background-color colors/white}
   [text-input/text-input-with-label
    {:on-change-text      #(re-frame/dispatch [:multiaccounts.recover/enter-phrase-input-changed (security/mask-data %)])
     :auto-focus          true
     :error               (when passphrase-error (i18n/label passphrase-error))
     :accessibility-label :passphrase-input
     :placeholder         nil
     :bottom-value        40
     :multiline           true
     :auto-correct        false
     :keyboard-type       "visible-password"
     :parent-container    {:flex 1
                           :align-self :stretch
                           :justify-content :center
                           :align-items :center}
     :container           {:background-color colors/white
                           :flex 1
                           :justify-content :center
                           :align-items :center}
     :style               (merge {:background-color    colors/white
                                  :text-align          :center
                                  :text-align-vertical :center
                                  :min-width 40
                                  :font-size        16
                                  :font-weight      "700"}
                                 (when platform/android?
                                   {:flex 1}))}]
   [react/view {:align-items :center}
    (if passphrase-word-count
      [react/view {:flex-direction :row
                   :margin-bottom 4
                   :min-height 24
                   :max-height 24
                   :align-items    :center}
       [react/nested-text {:style {:font-size    14
                                   :padding-right 4
                                   :text-align   :center
                                   :color        colors/gray}}
        (str (i18n/label  :t/word-count) ": ")
        [{:style {:font-weight       "500"
                  :color              colors/black}}
         (i18n/label-pluralize passphrase-word-count :t/words-n)]]
       (when-not next-button-disabled?
         [react/view {:style {:background-color colors/green-transparent-10
                              :border-radius 12
                              :width 24
                              :justify-content :center
                              :align-items :center
                              :height 24}}
          [vector-icons/tiny-icon :tiny-icons/tiny-check {:color colors/green}]])]
      [react/view {:align-self :stretch :margin-bottom 4
                   :max-height 24 :min-height 24}])
    [react/text {:style {:color      colors/gray
                         :font-size  14
                         :margin-bottom 8
                         :text-align :center}}
     (i18n/label :t/multiaccounts-recover-enter-phrase-text)]]
   (when processing?
     [react/view {:flex 1 :align-items :center}
      [react/activity-indicator {:size      :large
                                 :animating true}]
      [react/text {:style {:color      colors/gray
                           :margin-top 8}}
       (i18n/label :t/processing)]])])

(defn recovery-success [pubkey name photo-path]
  [react/view {:flex           1
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
      [react/text {:style           {:text-align  :center
                                     :margin-top  4
                                     :color       colors/gray
                                     :font-family "monospace"}
                   :number-of-lines 1
                   :ellipsize-mode  :middle}
       (utils/get-shortened-address pubkey)]]]]])

(defview wizard-generate-key []
  (letsubs [wizard-state [:intro-wizard/generate-key]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       {:icon    :main-icons/back
        :accessibility-label :back-button
        :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :generate-key}]
      [generate-key]
      [bottom-bar {:step :generate-key
                   :forward-action :intro-wizard/step-forward-pressed
                   :processing? (:processing? wizard-state)}]]]))

(defview wizard-choose-key []
  (letsubs [wizard-state [:intro-wizard/choose-key]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       {:label    :t/cancel
        :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
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
         {:label   :t/cancel
          :accessibility-label :back-button
          :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}
         {:icon    :main-icons/back
          :accessibility-label :back-button
          :handler #(re-frame/dispatch [:intro-wizard/navigate-back])})}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :select-key-storage}]
      [select-key-storage wizard-state]
      [bottom-bar {:step :select-key-storage
                   :forward-action (:forward-action wizard-state)}]]]))

(defview wizard-create-code []
  (letsubs [wizard-state [:intro-wizard/create-code]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       {:icon    :main-icons/back
        :accessibility-label :back-button
        :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :create-code :encrypt-with-password? (:encrypt-with-password? wizard-state)}]
      [create-code wizard-state]
      [bottom-bar (merge {:step :create-code
                          :forward-action (:forward-action wizard-state)}
                         wizard-state)]]]))

(defview wizard-confirm-code []
  (letsubs [wizard-state [:intro-wizard/confirm-code]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       (if (:processing? wizard-state)
         :none
         {:icon    :main-icons/back
          :accessibility-label :back-button
          :handler #(re-frame/dispatch [:intro-wizard/navigate-back])})}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :confirm-code :encrypt-with-password? (:encrypt-with-password? wizard-state)}]
      [confirm-code wizard-state]
      [bottom-bar (merge {:step :confirm-code
                          :forward-action (:forward-action wizard-state)}
                         wizard-state)]]]))

(defview wizard-enter-phrase []
  (letsubs [wizard-state [:intro-wizard/enter-phrase]]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       {:icon    :main-icons/back
        :accessibility-label :back-button
        :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex            1
                          :justify-content :space-between}}
      [top-bar {:step :enter-phrase}]
      [enter-phrase wizard-state]
      [bottom-bar (merge {:step :enter-phrase
                          :forward-action  :multiaccounts.recover/enter-phrase-next-pressed}
                         wizard-state)]]]))

(defview wizard-recovery-success []
  (letsubs [{:keys [pubkey processing? name photo-path]} [:intro-wizard/recovery-success]
            existing-account? [:intro-wizard/recover-existing-account?]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:navigation
       {:icon    :main-icons/back
        :accessibility-label :back-button
        :handler #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
     [react/view {:style {:flex 1
                          :justify-content :space-between}}
      [top-bar {:step :recovery-success}]
      [recovery-success pubkey name photo-path]
      [bottom-bar {:step              :recovery-success
                   :forward-action    :multiaccounts.recover/re-encrypt-pressed
                   :processing?       processing?
                   :existing-account? existing-account?}]]]))
