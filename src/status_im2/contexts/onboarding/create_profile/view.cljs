(ns status-im2.contexts.onboarding.create-profile.view
  (:require [quo2.core :as quo]
            [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.onboarding.create-profile.style :as style]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [react-native.hooks :as hooks]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [status-im2.contexts.onboarding.select-photo.method-menu.view :as method-menu]
            [utils.re-frame :as rf]
            [oops.core :as oops]
            [react-native.blur :as blur]
            [status-im2.constants :as c]
            [react-native.platform :as platform]))

;; NOTE - validation should match with Desktop
;; https://github.com/status-im/status-desktop/blob/2ba96803168461088346bf5030df750cb226df4c/ui/imports/utils/Constants.qml#L468
;; 
(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))
(defn has-emojis [s] (re-find emoji-regex s))
(def common-names ["Ethereum" "Bitcoin"])
(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))
(def status-regex (new js/RegExp #"^[a-zA-Z0-9\-_ ]+$"))
(defn has-special-characters [s] (not (re-find status-regex s)))
(def min-length 5)
(defn length-not-valid [s] (< (count (string/trim (str s))) min-length))

(defn validation-message
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth") (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth") (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (string/starts-with? s " ")  (i18n/label :t/start-with-space)
    (string/ends-with? s " ")    (i18n/label :t/ends-with-space)
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    :else                        nil))

(defn button-container
  [keyboard-shown? background? children]
  [rn/view {:style {:margin-top :auto}}
   (if keyboard-shown?
     (if background?
       [blur/ios-view
        (merge
         {:blur-amount      34
          :blur-type        :transparent
          :overlay-color    :transparent
          :background-color (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)
          :style            style/blur-button-container})
        children]
       [rn/view
        (merge
         {:style style/blur-button-container})
        children])
     [rn/view {:style style/view-button-container}
      children])])

(defn show-button-background
  [keyboard-shown? scroll-view-height visible-container-height]
  (let [{:keys [keyboard-height]} (hooks/use-keyboard)
        keyboard-button-height    (+ keyboard-height 64)
        remaining-screen-height   (- scroll-view-height keyboard-button-height)]
    (when keyboard-shown?
      (cond
        (< remaining-screen-height visible-container-height)
        true

        :else
        false))))

(defn- f-page
  [{:keys [onboarding-profile-data navigation-bar-top]}]
  (reagent/with-let [keyboard-shown?                         (reagent/atom false)
                     show-listener                           (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         (if platform/android?
                                                                           "keyboardDidShow"
                                                                           "keyboardWillShow")
                                                                         #(reset! keyboard-shown? true))
                     hide-listener                           (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         (if platform/android?
                                                                           "keyboardDidHide"
                                                                           "keyboardWillHide")
                                                                         #(reset! keyboard-shown? false))
                     {:keys [image-path display-name color]} onboarding-profile-data
                     full-name                               (reagent/atom display-name)
                     validation-msg                          (reagent/atom (validation-message
                                                                            @full-name))
                     on-change-text                          (fn [s]
                                                               (reset! validation-msg (validation-message
                                                                                       s))
                                                               (reset! full-name (string/trim s)))
                     custom-color                            (reagent/atom (or color
                                                                               c/profile-default-color))
                     profile-pic                             (reagent/atom image-path)
                     on-change-profile-pic                   #(reset! profile-pic %)
                     on-change                               #(reset! custom-color %)]
    (let [name-too-short?          (length-not-valid @full-name)
          valid-name?              (and (not @validation-msg) (not name-too-short?))
          info-message             (if @validation-msg
                                     @validation-msg
                                     (i18n/label :t/minimum-characters
                                                 {:min-chars min-length}))
          info-type                (cond @validation-msg :error
                                         name-too-short? :default
                                         :else           :success)
          scroll-view-height       (reagent/atom 0)
          visible-scroll-view-height (reagent/atom 0)
          background?              (show-button-background @keyboard-shown? @scroll-view-height @visible-scroll-view-height)]
      [rn/view {:style style/page-container}
       [navigation-bar/navigation-bar
        {:stack-id :new-to-status
         :top      navigation-bar-top}]
       [rn/scroll-view
        {:content-container-style {:flexGrow  1
                                   :on-layout #(reset! scroll-view-height
                                                 (-> ^js % .-nativeEvent .-layout .-height))}}
        [rn/view
         {:on-layout (fn [event]
                       (let [height      (oops/oget event "nativeEvent.layout")]
                         (js/console.log (str "view height " height ))
                         (reset! visible-scroll-view-height height)))}
         [rn/view
          {:style style/content-container}
          [quo/text
           {:size   :heading-1
            :weight :semi-bold
            :style  style/title} (i18n/label :t/create-profile)]
          [rn/view
           {:style style/input-container}
           [rn/view
            {:style style/profile-input-container}
            [quo/profile-input
             {:customization-color @custom-color
              :placeholder         (i18n/label :t/your-name)
              :on-press            (fn []
                                     (rf/dispatch [:dismiss-keyboard])
                                     (rf/dispatch
                                      [:show-bottom-sheet
                                       {:content
                                        (fn []
                                          [method-menu/view on-change-profile-pic])
                                        :theme :dark}]))
              :image-picker-props  {:profile-picture     (when @profile-pic {:uri @profile-pic})
                                    :full-name           (if (seq @full-name)
                                                           @full-name
                                                           (i18n/label :t/your-name))
                                    :customization-color @custom-color}
              :title-input-props   {:default-value  @full-name
                                    :auto-focus     true
                                    :max-length     c/profile-name-max-length
                                    :on-change-text on-change-text}}]]

           [quo/info-message
            {:type       info-type
             :size       :default
             :icon       (if valid-name? :i/positive-state :i/info)
             :text-color (when (= :default info-type) colors/white-70-blur)
             :icon-color (when (= :default info-type) colors/white-70-blur)
             :style      style/info-message}
            info-message]
           [quo/text
            {:size   :paragraph-2
             :weight :medium
             :style  style/color-title}
            (i18n/label :t/accent-colour)]
           [quo/color-picker
            {:blur?             true
             :default-selected? :blue
             :selected          @custom-color
             :on-change         on-change}]]]]]
       [rn/keyboard-avoiding-view
        {:style          {:position :absolute
                          :top      0
                          :bottom   0
                          :left     0
                          :right    0}
         :pointer-events :box-none}
        [button-container @keyboard-shown? background?
         [quo/button
          {:accessibility-label :submit-create-profile-button
           :type                :primary
           :customization-color @custom-color
           :on-press            (fn []
                                  (rf/dispatch [:onboarding-2/profile-data-set
                                                {:image-path   @profile-pic
                                                 :display-name @full-name
                                                 :color        @custom-color}]))
           :container-style     style/continue-button
           :disabled?           (or (not valid-name?) (not (seq @full-name)))}
          (i18n/label :t/continue)]]]])
    (finally
     (oops/ocall show-listener "remove")
     (oops/ocall hide-listener "remove"))))

(defn create-profile
  []
  (let [{:keys [top]}           (safe-area/get-insets)
        onboarding-profile-data (rf/sub [:onboarding-2/profile])]
    [:<>
     [:f> f-page
      {:navigation-bar-top      top
       :onboarding-profile-data onboarding-profile-data}]]))
