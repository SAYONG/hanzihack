(ns hanzihack.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]))

;;dispatchers

(rf/reg-event-db
  :navigate
  (fn [db [_ route]]
    (assoc db :route route)))

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))


(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))


(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

;;subscriptions

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

;; ----------------

(rf/reg-event-db
  :initial/set-table
  (fn [db [_ table]]
    (assoc db :initials (get-in table [:data :initials]))))

(rf/reg-event-fx
  :initial/fetch-table
  (fn [_ _]
    {:http-xhrio {:method          :post
                  :uri             "/api/graphql"
                  :headers         {:content-type "application/graphql"}
                  :body            "query getInitialTables{initials:initials{...initialWithActor}} fragment initialWithActor on initial{id,sound,group,pinyin,actor{id,name}}"
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:initial/set-table]}}))


(rf/reg-sub
  :initial/table
  (fn [db _]
    (let [initials (get-in db [:initials])]
      (vals
        (sort
          (apply merge-with merge
           (for [[sound [group data]] (map (juxt (comp keyword :sound) (fn [e] [((comp keyword :group) e) e])) initials)]
            {sound {:sound sound
                    group data}})))))))

