(ns status-im.contexts.communities.actions.detail-token-gating.view
 (:require 
  [quo.core :as quo]
  [quo.foundations.colors :as colors] 
  [react-native.core :as rn]
  [status-im.common.resources :as resources]
  [status-im.contexts.communities.actions.detail-token-gating.style :as style]
  [status-im.contexts.communities.actions.token-gating.view :as token-gating]
  [status-im.contexts.communities.utils :as communities.utils]
  [utils.i18n :as i18n]
  [utils.re-frame :as rf]))

 (def mock-token [{:symbol "STT", :sufficient? true, :loading? false, :amount "1.0000000000000000", :img-src (resources/get-mock-image :status-logo)}, {:symbol "STT", :sufficient? true, :loading? false, :amount "1.0000000000000000", :img-src (resources/get-mock-image :status-logo)}, {:symbol "STT", :sufficient? true, :loading? false, :amount "1.0000000000000000", :img-src (resources/get-mock-image :status-logo)}, {:symbol "STT", :sufficient? true, :loading? false, :amount "1.0000000000000000", :img-src (resources/get-mock-image :status-logo)}, {:symbol "STT", :sufficient? true, :loading? false, :amount "1.0000000000000000", :img-src (resources/get-mock-image :status-logo)}])
 (def mock-highest-permission-role 1)

(defn view
  [] 
  (let [{id :community-id} (rf/sub [:get-screen-params]) 
        {:keys [highest-permission-role tokens]} (rf/sub [:community/token-gated-overview id])
           highest-role-text
          (i18n/label
           (communities.utils/role->translation-key highest-permission-role :t/member))
         selected-addresses (rf/sub [:communities/selected-permission-addresses id])]
    
  [rn/view {:style style/container}
    [token-gating/token-requirements id]
   
     (when (and highest-permission-role (seq selected-addresses))
       [rn/view
        {:style style/highest-role}
        [quo/text
         {:size  :paragraph-2
          :style {:color colors/neutral-50}}
         (i18n/label :t/eligible-to-join-as {:role ""})]
        [quo/context-tag
         {:type    :icon
          :icon    :i/members
          :size    24
          :context highest-role-text}]])
  ;;  (tap> ["id" id])
  ;;  (tap> ["highest-permission-role" highest-permission-role])
   (tap> ["tokens" tokens])
   ]))

