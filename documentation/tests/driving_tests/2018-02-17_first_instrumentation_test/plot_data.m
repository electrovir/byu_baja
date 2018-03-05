clear;
csvData = csvread('sage_long.csv');

tach = csvData(:, 1);
speed = csvData(:, 2);
leftShock = csvData(:, 3);
rightShock = csvData(:, 4);

% some data points were garbled and thus removed from the data set
% this, this is not EXACTLY equivalent to seconds, by the end of the data
% set it's off by about 20 seconds
time = (1:length(tach)) / 10;

% fc = 1; % Cut off frequency
% fs = 10; % Sampling rate
% 
% [b,a] = butter(6,fc/(fs/2)); % Butterworth filter of order 6
% x = filter(b,a,speed); % Will be the filtered signal



figure(2);
clf;
plot (time, speed, 'displayname', 'speedometer'); hold on;
legend('location', 'best');

ylim([0, 100]);
ylabel('Speed (mph)');
xlabel('scaled data point number (roughly equivalent to seconds)');
xlim([min(time), max(time)]);
title('Speedometer Data');
set(gca, 'fontsize', 14);
i=4;
% for i = 1:10
%     last = plot(time, hampel(speed, i), 'color', 'red', 'linewidth', 2, 'displayname', string(i)); hold on;
%     pause(2);
%     delete(last);
% end

[envHigh, envLowFine] = envelope(speed,8,'peak');

[envHigh, envLowCoarse] = envelope(speed,20,'peak');

plot(time, [envLowFine(1:3600); envLowCoarse(3601:length(envLowCoarse))], 'displayname', 'smoothed', 'linewidth', 5, 'color', [1 0.4 0]);

%4

%%

% figure(2);
% clf;
% plot(time, leftShock, 'displayname', 'left'); hold on;
% plot(time, rightShock, 'displayname', 'right');
% xlim([min(time), max(time)]);
% ylim([0, 100]);
% ylabel('linear potentiometer travel (%)');
% xlabel('scaled data point number (roughly equivalent to seconds)');
% 
% 
% h = legend('show');
% % h = legend([a;b;c;d]);
% rect = [0.75, 0.3, 0.05, 0.05];
% set(h, 'Position', rect);
% 
% title('Shock Position Data');
% set(gca, 'fontsize', 14);