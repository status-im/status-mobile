(ns quo.components.wallet.keypair.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.icon-avatar :as icon-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.account-list-card.view :as account-list-card]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.components.wallet.keypair.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn keypair-string
  [full-name]
  (let [first-name (first (string/split full-name #" "))]
    (i18n/label :t/keypair-title {:name first-name})))

(defn avatar
  [{:keys               [blur?]
    {:keys [full-name]} :details
    avatar-type         :type
    customization-color :customization-color
    profile-picture     :profile-picture}]
  (if (= avatar-type :default-keypair)
    [user-avatar/user-avatar
     {:full-name           full-name
      :ring?               true
      :size                :small
      :status-indicator?   false
      :customization-color customization-color
      :profile-picture     profile-picture}]
    [icon-avatar/icon-avatar
     {:size    :size-32
      :icon    :i/seed
      :blur?   blur?
      :border? true}]))

(defn title-view
  [{:keys [details action selected? type blur? customization-color on-options-press]}]
  (let [theme               (quo.theme/use-theme)
        {:keys [full-name]} details]
    [rn/view
     {:style               style/title-container
      :accessibility-label :title}
     [text/text {:weight :semi-bold}
      (if (= type :default-keypair) (keypair-string full-name) full-name)]
     (case action
       :none     nil
       :selector [selectors/view
                  {:type                :radio
                   :checked?            selected?
                   :blur?               blur?
                   :customization-color customization-color}]
       [rn/pressable {:on-press on-options-press}
        [icon/icon :i/options
         {:color               (if blur?
                                 colors/white-opa-70
                                 (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))
          :accessibility-label :options-button}]])]))

(defn details-view
  [{:keys [details stored type blur? theme]}]
  (let [{:keys [address]} details]
    [rn/view
     {:style               {:flex-direction :row
                            :align-items    :center}
      :accessibility-label :details}
     [text/text
      {:size  :paragraph-2
       :style (style/subtitle blur? theme)}
      address]
     (when (= type :default-keypair)
       [text/text
        {:size  :paragraph-2
         :style (style/dot blur? theme)}
        " ∙ "])
     [text/text
      {:size  :paragraph-2
       :style (style/subtitle blur? theme)}
      (if (= stored :on-device) (i18n/label :t/on-device) (i18n/label :t/on-keycard))]
     (when (= stored :on-keycard)
       [rn/view {:style {:margin-left 4}}
        [icon/icon :i/keycard-card
         {:size  16
          :color (if blur?
                   colors/white-opa-40
                   (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]])]))

(defn- acc-list-card
  [item & _rest]
  [account-list-card/view item])

(defn view
  [{:keys [accounts action container-style selected? on-press] :as props}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:style          (style/container (assoc props
                                              :selected?       selected?
                                              :container-style container-style
                                              :theme           theme))
      :on-press       #(when (= action :selector) (on-press))
      :pointer-events (when (= action :selector) :box-only)}
     [rn/view {:style style/header-container}
      [avatar props]
      [rn/view
       {:style {:margin-left 8
                :flex        1}}
       [title-view (assoc props :selected? selected?)]
       [details-view props]]]
     [rn/flat-list
      {:data      accounts
       :render-fn acc-list-card
       :separator [rn/view {:style {:height 8}}]
       :style     {:padding-horizontal 8}}]]))
