(ns quo2.components.wallet.keypair.view
  (:require
    [clojure.string :as string]
    [quo2.components.avatars.icon-avatar :as icon-avatar]
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [quo2.components.icon :as icon]
    [quo2.components.list-items.account-list-card.view :as account-list-card]
    [quo2.components.markdown.text :as text]
    [quo2.components.selectors.selectors.view :as selectors]
    [quo2.components.wallet.keypair.style :as style]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))

(defn keypair-string
  [full-name]
  (let [first-name (first (string/split full-name #" "))]
    (i18n/label :t/keypair-title {:name first-name})))

(defn details-string
  [address stored]
  (str (when address (str address " ∙ "))
       (if (= stored :on-device) (i18n/label :t/on-device) (i18n/label :t/on-keycard))))

(defn avatar
  [{:keys [type details customization-color]}]
  (let [{:keys [full-name]} details]
    (if (= type :default-keypair)
      [user-avatar/user-avatar
       {:full-name           full-name
        :ring?               true
        :size                :small
        :customization-color customization-color}]
      [icon-avatar/icon-avatar
       {:size    :size/s-32
        :icon    :i/placeholder
        :border? true}])))

(defn title-view
  [{:keys [details action selected? type blur? customization-color on-options-press theme]}]
  (let [{:keys [full-name]} details]
    [rn/view
     {:style               style/title-container
      :accessibility-label :title}
     [text/text {:weight :semi-bold}
      (if (= type :default-keypair) (keypair-string full-name) full-name)]
     (if (= action :selector)
       [selectors/radio
        {:checked?            selected?
         :blur?               blur?
         :customization-color customization-color}]
       [rn/pressable {:on-press on-options-press}
        [icon/icon :i/options
         {:color               (if blur?
                                 colors/white-opa-70
                                 (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))
          :accessibility-label :options-button}]])]))

(defn details-view
  [{:keys [details stored blur? theme]}]
  (let [{:keys [address]} details]
    [rn/view
     {:style {:flex-direction :row
              :align-items    :center}}
     [text/text
      {:size                :paragraph-2
       :accessibility-label :details
       :style               {:color (if blur?
                                      colors/white-opa-40
                                      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}}
      (details-string address stored)]
     (when (= stored :on-keycard)
       [rn/view {:style {:margin-left 4}}
        [icon/icon :i/keycard-card
         {:size  16
          :color (if blur?
                   colors/white-opa-40
                   (colors/theme-colors colors/neutral-50 colors/neutral-40))}]])]))

(defn- view-internal
  []
  (let [selected? (reagent/atom true)]
    (fn [{:keys [accounts action] :as props}]
      [rn/pressable
       {:style    (style/container (merge props {:selected? @selected?}))
        :on-press #(when (= action :selector) (reset! selected? (not @selected?)))}
       [rn/view {:style style/header-container}
        [avatar props]
        [rn/view
         {:style {:margin-left 8
                  :flex        1}}
         [title-view (assoc props :selected? @selected?)]
         [details-view props]]]
       [rn/flat-list
        {:data      accounts
         :render-fn account-list-card/view
         :separator [rn/view {:style {:height 8}}]
         :style     {:padding-horizontal 8}}]])))

(def view (quo.theme/with-theme view-internal))
