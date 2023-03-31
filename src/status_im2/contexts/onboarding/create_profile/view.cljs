(ns status-im2.contexts.onboarding.create-profile.view
  (:require [quo2.core :as quo]
            [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.onboarding.create-profile.style :as style]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.select-photo.method-menu.view :as method-menu]
            [utils.re-frame :as rf]
            [oops.core :as oops]
            [react-native.blur :as blur]
            [status-im2.constants :as c]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:container-style {:padding-horizontal 0}
     :align-mid?      true
     :mid-section     {:type :text-only :main-text ""}
     :left-section    {:type                :blur-bg
                       :icon                :i/arrow-left
                       :icon-override-theme :dark
                       :on-press            #(rf/dispatch [:navigate-back])}}]])

(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))
(defn has-emojis [s] (re-find emoji-regex s))
(def common-names ["Ethereum" "Bitcoin"])
(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))
(def special-characters-regex (new js/RegExp #"[^a-zA-Z\d\s-._]" "i"))
(defn has-special-characters [s] (re-find special-characters-regex s))
(def min-length 5)
(defn length-not-valid [s] (< (count (string/trim s)) min-length))
(def valid-regex (new js/RegExp #"^[\w-\s]{5,24}$" "i"))
(defn valid-name [s] (re-find valid-regex s))

(defn validation-message
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth") (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth") (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    (length-not-valid s)         (i18n/label :t/name-must-have-at-least-characters
                                             {:min-chars min-length})
    (not (valid-name s))         (i18n/label :t/name-is-not-valid)
    :else                        nil))

(defn button-container
  [keyboard-shown? children]
  (if keyboard-shown?
    [blur/ios-view
     {:style style/blur-button-container}
     children]
    [rn/view {:style style/view-button-container}
     children]))

(defn page
  [{:keys [image-path display-name color]}]
  [:f>
   (fn []
     (let [full-name             (reagent/atom display-name)
           keyboard-shown?       (reagent/atom false)
           validation-msg        (reagent/atom (validation-message @full-name))
           on-change-text        (fn [s]
                                   (reset! validation-msg (validation-message s))
                                   (reset! full-name (string/trim s)))
           custom-color          (reagent/atom (or color c/profile-default-color))
           profile-pic           (reagent/atom image-path)
           on-change-profile-pic #(reset! profile-pic %)
           on-change             #(reset! custom-color %)]
       (fn []
         (rn/use-effect
          (let [will-show-listener (oops/ocall rn/keyboard
                                               "addListener"
                                               "keyboardWillShow"
                                               #(swap! keyboard-shown? (fn [] true)))
                will-hide-listener (oops/ocall rn/keyboard
                                               "addListener"
                                               "keyboardWillHide"
                                               #(swap! keyboard-shown? (fn [] false)))]
            (fn []
              (fn []
                (oops/ocall will-show-listener "remove")
                (oops/ocall will-hide-listener "remove"))))
          [])
         [rn/view {:style style/page-container}
          [rn/scroll-view
           {:keyboard-should-persist-taps :always
            :content-container-style      {:flex-grow 1}}
           [rn/view {:style style/page-container}
            [rn/view
             {:style style/content-container}
             [navigation-bar]
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
                 :on-press            #(rf/dispatch
                                        [:bottom-sheet/show-sheet
                                         {:override-theme :dark
                                          :content
                                          (fn []
                                            [method-menu/view on-change-profile-pic])}])
                 :image-picker-props  {:profile-picture @profile-pic
                                       :full-name       @full-name}
                 :title-input-props   {:default-value  @full-name
                                       :max-length     c/profile-name-max-length
                                       :on-change-text on-change-text}}]]
              (when @validation-msg
                [quo/info-message
                 {:type  :error
                  :size  :default
                  :icon  :i/info
                  :style style/info-message}
                 @validation-msg])
              [quo/text
               {:size  :paragraph-2
                :style style/color-title}
               (i18n/label :t/accent-colour)]
              [quo/color-picker
               {:blur?             true
                :default-selected? :blue
                :selected          @custom-color
                :on-change         on-change}]]]]]
          [rn/keyboard-avoiding-view {}
           [button-container @keyboard-shown?
            [quo/button
             {:accessibility-label       :submit-create-profile-button
              :type                      :primary
              :override-background-color (colors/custom-color @custom-color 60)
              :on-press                  (fn []
                                           (rf/dispatch [:onboarding-2/profile-data-set
                                                         {:image-path   @profile-pic
                                                          :display-name @full-name
                                                          :color        @custom-color}]))
              :style                     style/continue-button
              :disabled                  (or (not (seq @full-name)) @validation-msg)}
             (i18n/label :t/continue)]]]])))])

(defn create-profile
  []
  (let [onboarding-profile-data (rf/sub [:onboarding-2/profile])]
    [:<>
     [background/view true]
     [page onboarding-profile-data]]))
