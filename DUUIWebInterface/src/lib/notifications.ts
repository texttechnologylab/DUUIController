import type { Unsubscriber } from 'svelte/motion';
import { derived, writable, type Writable } from 'svelte/store';

enum NotifactionType {
	Danger = '#E26D69',
	Info = '#5bc0de',
	Success = '#84C991',
	Warning = '#f0ad4e'
}

interface Notification {
	id: string;
	type: NotifactionType;
	title: string;
	message: string;
	timeout: number;
	icon: string;
}

function createNotificationStore(timeout: number = 1000) {
	const _notifications: Writable<Notification[]> = writable([]);

	function send(
		title: string,
		message: string,
		type = NotifactionType.Info,
		timeout: number,
		icon: string
	) {
		_notifications.update((state: Notification[]) => {
			return [...state, { id: id(), type, title, message, timeout, icon }];
		});
	}

	let timers = [];

	const notifications = derived(_notifications, ($_notifications, set) => {
		set($_notifications);
		if ($_notifications.length > 0) {
			const timer = setTimeout(() => {
				_notifications.update((state) => {
					state.shift();
					return state;
				});
			}, $_notifications[0].timeout);
			return () => {
				clearTimeout(timer);
			};
		}
	});
	const { subscribe } = notifications;

	return {
		subscribe,
		send,
		danger: (title: string, message: string, timeout: number) =>
			send(title, message, NotifactionType.Danger, timeout, 'close-solid'),

		warning: (title: string, message: string, timeout: number) =>
			send(title, message, NotifactionType.Warning, timeout, 'exclamation-circle-solid'),

		info: (title: string, message: string, timeout: number) =>
			send(title, message, NotifactionType.Info, timeout, 'bell-active-solid'),

		success: (title: string, message: string, timeout: number) =>
			send(title, message, NotifactionType.Success, timeout, 'check-solid')
	};
}

function id() {
	return '_' + Math.random().toString(36).substr(2, 9);
}

export const notifications = createNotificationStore();
