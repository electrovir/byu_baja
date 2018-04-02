clear;
data = csvread('data/mini_run_4.csv');

startTime = data(1, 8);
time = data(:, 8) - startTime;

speed = data(:, 4)';
x = 0:length(speed) - 1;

figure(1);
clf;
subplot(2, 1, 1);
plot(x, speed, 'displayname', 'speed reading', 'linewidth', 3);
hold on;
plot(x, time / 100, 'displayname', 'hundreds of seconds', 'linewidth', 3);
legend('show', 'location', 'best');
ylim([-1, 10]);
title('Driving Test: noise (no magnet) Mar 30, 2018 - Casey');
xlabel('Entry Number');
ylabel('Value');
set(gca, 'fontsize', 14);

data = csvread('data/mini_run_3.csv');

startTime = data(1, 8);
time = data(:, 8) - startTime;

speed = data(:, 4)';
x = 0:length(speed) - 1;

subplot(2, 1, 2);
plot(x, speed, 'displayname', 'speed reading', 'linewidth', 3);
hold on;
plot(x, time / 100, 'displayname', 'hundreds of seconds', 'linewidth', 3);
legend('show', 'location', 'best');
ylim([-1, 10]);
title('Driving Test: noise (no magnet) Mar 30, 2018 - Nolan');
xlabel('Entry Number');
ylabel('Value');
set(gca, 'fontsize', 14);