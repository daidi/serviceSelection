package com.daidi.ssme;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import javax.swing.*;

public class beeColony extends JFrame implements ActionListener
{
	/* ������� */
	JFrame frame;
	JTextField JT1;
	Label prompt;
	Label lblTime;
	TextArea txtLogs;
	Button btnOpen, btnStart, btnVisual1, btnVisual2;
	JFileChooser chooser;
	File file = null;
	JComboBox cbMethod;
	ImageIcon  ic;
	 
	/* ABC�㷨�Ŀ��Ʋ��� */
	int NP = 20; // ��Ⱥ���� (��Ӷ��+�۲��)
	int FoodNumber = NP / 2; // ʳ��Դ������Ӧ���Ƿ�Ⱥ������һ��
	int limit = 5000; // �����Ӷ��ͨ��limit�γ��Ժ���Ȼδ�����ĳʳ��Դ������������Ӷ��ͱ��Ϊ���䣬��ӵ�еĽ�ͻᱻ����
	int maxCycle = 2500; // ��ʳ��ֹͣ��ֵ

	/* ���ⶨ����� */
	int d = 14; // Ŀ�������ά��
	float maxRT = 2500;
	float minRel = 0.7f;
	float minTP = 30;
	float maxCost = 400;
	float ResponseTime[] = new float[5];
	float Reliability[] = new float[5];
	float Throughput[] = new float[5];
	float Cost[] = new float[5];

	int Foods[][] = new int[FoodNumber][d]; // ʳ��Դ�ĸ���
	double f[] = new double[FoodNumber]; // ����ʳ��Դ��Ŀ�꺯��ֵ
	double fitness[] = new double[FoodNumber]; // ʳ��Դ����Ӧ��
	int trial[] = new int[FoodNumber]; // ��¼���Դ�����ͨ���������ж�solution�Ƿ����
	double prob[] = new double[FoodNumber]; // ��¼ʳ��
	int solution[] = new int[d]; // �µĽⷨ v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})
									// j �����ѡ�������k����i��ͬ�������
	private int serveMethod = 1;
	ArrayList<Float> ansList = new ArrayList<Float>();
	double ObjValSol; // �½ⷨ��Ŀ�꺯��ֵ
	double FitnessSol; // �½ⷨ����Ӧ��

	double GlobalMin; /* Optimum solution obtained by ABC algorithm */
	int GlobalParams[][] = new int[5][d]; /* Parameters of the optimum solution */
	double r; /* a random number in the range [0,1) */

	float A[][] = new float[500][4];
	float B[][] = new float[500][4];
	float C[][] = new float[500][4];
	float D[][] = new float[500][4];
	float E[][] = new float[500][4];
	float F[][] = new float[500][4];
	float G[][] = new float[500][4];
	float H[][] = new float[500][4];
	float I[][] = new float[500][4];
	float J[][] = new float[500][4];
	float K[][] = new float[500][4];
	float L[][] = new float[500][4];
	float M[][] = new float[500][4];
	float N[][] = new float[500][4];

	long time=0;
	
	public void start()
	{
		frame = new JFrame("����ѡ��");
		JT1 = new JTextField();
		// frame.add(JT1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		prompt = new Label("������¼��Logs����");
		lblTime=new Label("ִ�к�ʱ�� ��δ��ʼִ��");
		txtLogs = new TextArea();
		txtLogs.setEditable(false);
		btnOpen = new Button("ѡ���ļ�·��");
		btnStart = new Button("��ʼ");
		btnVisual1 = new Button("��ʾʱ��");
		btnVisual2 = new Button("���ܶԱ�");
		frame.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 40));
		String[] serveStrings =
		{ "��һ����������", "�ڶ�����������", "��������������", "���ĸ���������" };
		cbMethod = new JComboBox(serveStrings);
		cbMethod.setSelectedIndex(0);
		final JLabel jl=new JLabel();//��ͼƬ���ڱ�ǩ��
		
		frame.add(btnOpen);
		frame.add(cbMethod);
		frame.add(btnStart);
		frame.add(btnVisual1);
		frame.add(btnVisual2);
		frame.add(lblTime);
		frame.add(jl);
		frame.add(prompt);
		frame.add(txtLogs);
		

		btnOpen.addActionListener(this);
		btnStart.addActionListener(this);
		btnVisual1.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				lblTime.setText("ִ�к�ʱ��  "+time/1000f+"  ��");				
			}
		});
		btnVisual2.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				

				visual(1, ansList);
				ansList.clear();
				ic=new ImageIcon("C:/result.jpg");
				jl.setIcon(ic);
				
			}


		});
		cbMethod.addActionListener(this);
		frame.setSize(600, 900);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{

		if (e.getSource() == btnOpen)
		{
			chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);// ֻ��ѡ���ļ�
			int flag = chooser.showOpenDialog(this);
			if (flag == JFileChooser.APPROVE_OPTION)
			{
				file = chooser.getSelectedFile();

			}
		}
		else if (e.getSource() == cbMethod)
		{
			serveMethod = cbMethod.getSelectedIndex() + 1;
		}
		else if (e.getSource() == btnStart)
		{
			if (file == null)
			{
				JOptionPane.showMessageDialog(this, "<html>��ѡ��·������</html>",
						"Note", JOptionPane.WARNING_MESSAGE);
				return;
			}
			switch (serveMethod)
			{
			case 1:
				maxRT = 2500;
				minRel = 0.7f;
				minTP = 30;
				maxCost = 400;
				break;
			case 2:
				maxRT = 3000;
				minRel = 0.5f;
				minTP = 30;
				maxCost = 500;
				break;
			case 3:
				maxRT = 4500;
				minRel = 0.3f;
				minTP = 30;
				maxCost = 8000;
				break;
			case 4:
				maxRT = 5000;
				minRel = 0.3f;
				minTP = 30;
				maxCost = 9000;
				break;
			default:
				txtLogs.append("\n" + "��ֵ���ִ�������������̣�");
			}

			loadData(file);
			time=System.currentTimeMillis();
			for (int i = 0; i < 5; i++)
			{
				mainThread();
			}
			txtLogs.append("\n�����С���");
			float sumCost = 0;
			if (serveMethod == 1)
			{
				for (int i = 0; i < 5; i++)
				{
					for (int j = 0; j < 5; j++)
					{
						txtLogs.append("\nִ�гɹ���");
						txtLogs.append("\n");
					}
				}
			}
			else if (serveMethod == 2)
			{
				for (int i = 0; i < 5; i++)
				{
					for (int j = 0; j < 5; j++)
					{
						txtLogs.append("\nִ�гɹ���");
						txtLogs.append("\n");
					}

				}
			}
			else if (serveMethod == 3)
			{
				for (int i = 0; i < 5; i++)
				{
					for (int j = 0; j < 5; j++)
					{
					}
					txtLogs.append("\nִ�гɹ���");
					txtLogs.append("\n");
				}
			}
			else if (serveMethod == 4)
			{
				for (int i = 0; i < 5; i++)
				{
					for (int j = 0; j < 5; j++)
					{
					}
					txtLogs.append("\nִ�гɹ���");
					txtLogs.append("\n");
				}
			}
			time=System.currentTimeMillis()-time;
		}
	}

	private void mainThread()
	{
		initial();
		MemorizeBestSource();
		for (int iter = 0; iter < maxCycle; iter++)
		{
			SendEmployedBees();
			CalculateProbabilities();
			SendOnlookerBees();
			MemorizeBestSource();
			SendScoutBees();
		}
		ResponseTime[0] = A[GlobalParams[0][0]][0]
				+ B[GlobalParams[0][1]][0] + C[GlobalParams[0][2]][0]
				+ D[GlobalParams[0][3]][0] + G[GlobalParams[0][4]][0];
		Reliability[0] = A[GlobalParams[0][0]][1]
				+ B[GlobalParams[0][1]][1] + C[GlobalParams[0][2]][1]
				+ D[GlobalParams[0][3]][1] + G[GlobalParams[0][4]][1];
		Throughput[0] = A[GlobalParams[0][0]][2]
				+ B[GlobalParams[0][1]][2] + C[GlobalParams[0][2]][2]
				+ D[GlobalParams[0][3]][2] + G[GlobalParams[0][4]][2];
		Cost[0] = A[GlobalParams[0][0]][3] + B[GlobalParams[0][1]][3]
				+ C[GlobalParams[0][2]][3] + D[GlobalParams[0][3]][3]
				+ G[GlobalParams[0][4]][3];
		ansList.add(ResponseTime[0]);
		ansList.add(Reliability[0]);
		ansList.add(Throughput[0]);
		ansList.add(Cost[0]);
		if(serveMethod==1)
		{
			txtLogs.append("\n**********************");
			txtLogs.append("\nA:ѡ�����A-"+(GlobalParams[0][0]+1));
			txtLogs.append("\nB:ѡ�����B-"+(GlobalParams[0][1]+1));
			txtLogs.append("\nC:ѡ�����C-"+(GlobalParams[0][2]+1));
			txtLogs.append("\nD:ѡ�����D-"+(GlobalParams[0][3]+1));
			txtLogs.append("\nG:ѡ�����G-"+(GlobalParams[0][4]+1));
			txtLogs.append("\n**********************\n");
		}
		else if(serveMethod==2)
		{
			txtLogs.append("\n**********************");
			txtLogs.append("\nA:ѡ�����A-"+(GlobalParams[0][0]+1));
			txtLogs.append("\nB:ѡ�����B-"+(GlobalParams[0][1]+1));
			txtLogs.append("\nC:ѡ�����C-"+(GlobalParams[0][2]+1));
			txtLogs.append("\nD:ѡ�����D-"+(GlobalParams[0][3]+1));
			txtLogs.append("\nE:ѡ�����E-"+(GlobalParams[0][4]+1));
			txtLogs.append("\nG:ѡ�����G-"+(GlobalParams[0][5]+1));
			txtLogs.append("\nH:ѡ�����H-"+(GlobalParams[0][6]+1));
			txtLogs.append("\nK:ѡ�����K-"+(GlobalParams[0][7]+1));
			txtLogs.append("\n**********************\n");
		}
		else if(serveMethod==3)
		{
			txtLogs.append("\n**********************");
			txtLogs.append("\nA:ѡ�����A-"+(GlobalParams[0][0]+1));
			txtLogs.append("\nB:ѡ�����B-"+(GlobalParams[0][1]+1));
			txtLogs.append("\nC:ѡ�����C-"+(GlobalParams[0][2]+1));
			txtLogs.append("\nD:ѡ�����D-"+(GlobalParams[0][3]+1));
			txtLogs.append("\nE:ѡ�����E-"+(GlobalParams[0][4]+1));
			txtLogs.append("\nF:ѡ�����F-"+(GlobalParams[0][5]+1));
			txtLogs.append("\nG:ѡ�����G-"+(GlobalParams[0][6]+1));
			txtLogs.append("\nH:ѡ�����H-"+(GlobalParams[0][7]+1));
			txtLogs.append("\nI:ѡ�����I-"+(GlobalParams[0][8]+1));
			txtLogs.append("\nJ:ѡ�����J-"+(GlobalParams[0][9]+1));
			txtLogs.append("\nK:ѡ�����K-"+(GlobalParams[0][10]+1));
			txtLogs.append("\nL:ѡ�����L-"+(GlobalParams[0][11]+1));
			txtLogs.append("\n**********************\n");
		}
		else if(serveMethod==4)
		{
			txtLogs.append("\n**********************");
			txtLogs.append("\nA:ѡ�����A-"+(GlobalParams[0][0]+1));
			txtLogs.append("\nB:ѡ�����B-"+(GlobalParams[0][1]+1));
			txtLogs.append("\nC:ѡ�����C-"+(GlobalParams[0][2]+1));
			txtLogs.append("\nD:ѡ�����D-"+(GlobalParams[0][3]+1));
			txtLogs.append("\nE:ѡ�����E-"+(GlobalParams[0][4]+1));
			txtLogs.append("\nF:ѡ�����F-"+(GlobalParams[0][5]+1));
			txtLogs.append("\nG:ѡ�����G-"+(GlobalParams[0][6]+1));
			txtLogs.append("\nH:ѡ�����H-"+(GlobalParams[0][7]+1));
			txtLogs.append("\nI:ѡ�����I-"+(GlobalParams[0][8]+1));
			txtLogs.append("\nJ:ѡ�����J-"+(GlobalParams[0][9]+1));
			txtLogs.append("\nK:ѡ�����K-"+(GlobalParams[0][10]+1));
			txtLogs.append("\nL:ѡ�����L-"+(GlobalParams[0][11]+1));
			txtLogs.append("\nM:ѡ�����M-"+(GlobalParams[0][12]+1));
			txtLogs.append("\nN:ѡ�����N-"+(GlobalParams[0][13]+1));
			txtLogs.append("\n**********************\n");
		}
		
	}

	public void loadData(File file)
	{
		String str = null;
		StringTokenizer st;
		BufferedReader br = null;
		StringBuilder txt = new StringBuilder();
		try
		{
			br = new BufferedReader(new FileReader(file));
			str = br.readLine();
			while (str != null)
			{
				txt.append(str);
				str = br.readLine();
			}
			br.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		st = new StringTokenizer(txt.toString());
		int i = 0;
		while (st.hasMoreTokens())
		{
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				A[i][0] = Float.parseFloat(st.nextToken());
				A[i][1] = Float.parseFloat(st.nextToken());
				A[i][2] = Float.parseFloat(st.nextToken());
				A[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				B[i][0] = Float.parseFloat(st.nextToken());
				B[i][1] = Float.parseFloat(st.nextToken());
				B[i][2] = Float.parseFloat(st.nextToken());
				B[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				C[i][0] = Float.parseFloat(st.nextToken());
				C[i][1] = Float.parseFloat(st.nextToken());
				C[i][2] = Float.parseFloat(st.nextToken());
				C[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				D[i][0] = Float.parseFloat(st.nextToken());
				D[i][1] = Float.parseFloat(st.nextToken());
				D[i][2] = Float.parseFloat(st.nextToken());
				D[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				E[i][0] = Float.parseFloat(st.nextToken());
				E[i][1] = Float.parseFloat(st.nextToken());
				E[i][2] = Float.parseFloat(st.nextToken());
				E[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				F[i][0] = Float.parseFloat(st.nextToken());
				F[i][1] = Float.parseFloat(st.nextToken());
				F[i][2] = Float.parseFloat(st.nextToken());
				F[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				G[i][0] = Float.parseFloat(st.nextToken());
				G[i][1] = Float.parseFloat(st.nextToken());
				G[i][2] = Float.parseFloat(st.nextToken());
				G[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				H[i][0] = Float.parseFloat(st.nextToken());
				H[i][1] = Float.parseFloat(st.nextToken());
				H[i][2] = Float.parseFloat(st.nextToken());
				H[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				I[i][0] = Float.parseFloat(st.nextToken());
				I[i][1] = Float.parseFloat(st.nextToken());
				I[i][2] = Float.parseFloat(st.nextToken());
				I[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				J[i][0] = Float.parseFloat(st.nextToken());
				J[i][1] = Float.parseFloat(st.nextToken());
				J[i][2] = Float.parseFloat(st.nextToken());
				J[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				K[i][0] = Float.parseFloat(st.nextToken());
				K[i][1] = Float.parseFloat(st.nextToken());
				K[i][2] = Float.parseFloat(st.nextToken());
				K[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				L[i][0] = Float.parseFloat(st.nextToken());
				L[i][1] = Float.parseFloat(st.nextToken());
				L[i][2] = Float.parseFloat(st.nextToken());
				L[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				M[i][0] = Float.parseFloat(st.nextToken());
				M[i][1] = Float.parseFloat(st.nextToken());
				M[i][2] = Float.parseFloat(st.nextToken());
				M[i][3] = Float.parseFloat(st.nextToken());
			}
			for (i = 0; i < 500; i++)
			{
				st.nextToken();
				N[i][0] = Float.parseFloat(st.nextToken());
				N[i][1] = Float.parseFloat(st.nextToken());
				N[i][2] = Float.parseFloat(st.nextToken());
				N[i][3] = Float.parseFloat(st.nextToken());
			}

		}
	}

	public static CategoryDataset getDataSet(int type, ArrayList<Float> value)
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(value.get(0), "Response Time", "��һ");
		dataset.addValue(value.get(1), "Reliability", "��һ");
		dataset.addValue(value.get(2), "Throughput", "��һ");
		dataset.addValue(value.get(3), "Cost", "��һ");
		dataset.addValue(value.get(4), "Response Time", "���");
		dataset.addValue(value.get(5), "Reliability", "���");
		dataset.addValue(value.get(6), "Throughput", "���");
		dataset.addValue(value.get(7), "Cost", "���");
		dataset.addValue(value.get(8), "Response Time", "����");
		dataset.addValue(value.get(9), "Reliability", "����");
		dataset.addValue(value.get(10), "Throughput", "����");
		dataset.addValue(value.get(11), "Cost", "����");
		dataset.addValue(value.get(12), "Response Time", "����");
		dataset.addValue(value.get(13), "Reliability", "����");
		dataset.addValue(value.get(14), "Throughput", "����");
		dataset.addValue(value.get(15), "Cost", "����");
		dataset.addValue(value.get(16), "Response Time", "����");
		dataset.addValue(value.get(17), "Reliability", "����");
		dataset.addValue(value.get(18), "Throughput", "����");
		dataset.addValue(value.get(19), "Cost", "����");
		return dataset;
	}

	public static void main(String[] s)
	{
		beeColony bee = new beeColony();
		bee.start();
	}

	private void visual(int type, ArrayList<Float> value)
	{
		CategoryDataset dataset = getDataSet(type, value);
		// ����������ʽ
		StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
		// ���ñ�������
		standardChartTheme.setExtraLargeFont(new Font("����", Font.BOLD, 20));
		// ����ͼ��������
		standardChartTheme.setRegularFont(new Font("����", Font.PLAIN, 15));
		// �������������
		standardChartTheme.setLargeFont(new Font("����", Font.PLAIN, 15));
		// Ӧ��������ʽ
		ChartFactory.setChartTheme(standardChartTheme);
		JFreeChart chart = ChartFactory.createBarChart3D("���ܶԱ�ͼ", // ͼ�����
				"����", // Ŀ¼�����ʾ��ǩ
				"QoSָ��", // ��ֵ�����ʾ��ǩ
				dataset, // ���ݼ�
				PlotOrientation.VERTICAL, // ͼ����ˮƽ����ֱ
				true, // �Ƿ���ʾͼ��(���ڼ򵥵���״ͼ������false)
				false, // �Ƿ����ɹ���
				false // �Ƿ�����URL����
				);
chart.clearSubtitles();
		FileOutputStream fos_jpg = null;
		try
		{
			fos_jpg = new FileOutputStream("C://result.jpg");
			ChartUtilities.writeChartAsJPEG(fos_jpg, 1, chart, 400, 300, null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				fos_jpg.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/* ��Ӧ�ȼ��� */
	double CalculateFitness(double fun)
	{
		double result = 0;
		if (fun >= 0)
		{
			result = 1 / (fun + 1);
		}
		else
		{
			result = 1 + Math.abs(fun);
		}
		return result;
	}

	/* ��¼���ʳ��Դ */
	void MemorizeBestSource()
	{
		int i, j, k;
		for (i = 0; i < FoodNumber; i++)
		{

			GlobalMin = calculateFunction(GlobalParams[0]);
			int flag = 0;
			for (k = 1; k < 5; k++)
			{
				if (GlobalMin < calculateFunction(GlobalParams[k]))
				{
					GlobalMin = calculateFunction(GlobalParams[k]);
					flag = k;
				}
			}
			GlobalParams[flag] = solution;

			GlobalMin = f[0];

			if (f[i] < GlobalMin) // Ҫ��Ŀ�꺯��ֵԽСԽ�� ���������ԽСԽ��
			{
				for (j = 0; j < d; j++)
					GlobalParams[flag][j] = Foods[i][j];
				GlobalMin = f[i];
			}

		}
	}

	/*Variables are initialized in the range [lb,ub]. If each parameter has different range, use arrays lb[j], ub[j] instead of lb and ub */
	/* Counters of food sources are also initialized in this function*/
	void init(int index)
	{
		int j, r;
		for (j = 0; j < d; j++)
		{
			r = (int) (Math.random() * 500); // ����0~499
			Foods[index][j] = r; // ��ȡ���������±�ֵ
		}
		f[index] = calculateFunction(Foods[index]);
		fitness[index] = CalculateFitness(f[index]);
		trial[index] = 0;
		while (!isSolution(Foods, index))
		{
			init(index);
		}
	}

	// �����Ƿ���������
	public boolean isSolution(int Foods[][], int i)
	{
		float totalTime = 0;
		float totalReliability = 0;
		float totalThroughput = 0;
		float totalCost = 0;
		if (serveMethod == 1)
		{
			/*
			 * A[Foods[i][0]][0] B[Foods[i][1][0] C[Foods[i][2]][0]
			 * D[Foods[i][3]][0] G[Foods[i][4]][0]
			 */
			float temp = C[Foods[i][2]][0] + D[Foods[i][3]][0];
			if (B[Foods[i][1]][0] < temp)
			{
				totalTime = A[Foods[i][0]][0] + temp + G[Foods[i][4]][0];
			}
			else
			{
				totalTime = A[Foods[i][0]][0] + B[Foods[i][1]][0]
						+ G[Foods[i][4]][0];
			}
			totalReliability = A[Foods[i][0]][1] * B[Foods[i][1]][1]
					* C[Foods[i][2]][1] * D[Foods[i][3]][1] * G[Foods[i][4]][1];
			totalCost = A[Foods[i][0]][3] + B[Foods[i][1]][3]
					+ C[Foods[i][2]][3] + D[Foods[i][3]][3] + G[Foods[i][4]][3];
			float ta = A[Foods[i][0]][2];
			float tb = B[Foods[i][1]][2];
			float tc = C[Foods[i][2]][2];
			float td = D[Foods[i][3]][2];
			float tg = G[Foods[i][4]][2];
			float tempt1 = 0, tempt2 = 0, tempt3 = 0;
			if (ta <= tb)
				tempt1 = ta;
			else
				tempt1 = tb;
			if (tc <= td)
				tempt2 = tc;
			else
				tempt2 = td;
			if (tempt1 <= tempt2)
				tempt3 = tempt1;
			else
				tempt3 = tempt2;
			if (tempt3 <= tg)
				totalThroughput = tempt3;
			else
				totalThroughput = tg;
			if (totalTime <= maxRT
					&& totalReliability >= minRel
					&& totalThroughput >= minTP && totalCost <= maxCost)
				return true;
			else
				return false;
		}
		else if (serveMethod == 2)
		{
			/*
			 * A[Foods[i][0]][0] B[Foods[i][1]][0] C[Foods[i][2]][0]
			 * D[Foods[i][3]][0] E[Foods[i][4]][0] G[Foods[i][5]][0]
			 * H[Foods[i][6]][0] K[Foods[i][7]][0]
			 */
			float time1 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ E[Foods[i][4]][0] + H[Foods[i][6]][0] + K[Foods[i][7]][0];
			float time2 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ D[Foods[i][3]][0] + G[Foods[i][5]][0] + K[Foods[i][7]][0];
			float time3 = A[Foods[i][0]][0] + B[Foods[i][1]][0]
					+ G[Foods[i][3]][0] + K[Foods[i][7]][0];
			if (time1 < time2)
				totalTime = time2;
			else
				totalTime = time1;
			if (totalTime < time3)
				totalTime = time3;
			totalReliability = A[Foods[i][0]][1] * B[Foods[i][1]][1]
					* C[Foods[i][2]][1] * D[Foods[i][3]][1] * E[Foods[i][4]][1]
					* G[Foods[i][5]][1] * H[Foods[i][6]][1] * K[Foods[i][7]][1];
			totalCost = A[Foods[i][0]][1] + B[Foods[i][1]][1]
					+ C[Foods[i][2]][1] + D[Foods[i][3]][1] + E[Foods[i][4]][1]
					+ G[Foods[i][5]][1] + H[Foods[i][6]][1] + K[Foods[i][7]][1];
			float[] t = new float[8];
			totalThroughput = A[Foods[i][0]][2];
			t[1] = B[Foods[i][1]][2];
			t[2] = C[Foods[i][2]][2];
			t[3] = D[Foods[i][3]][2];
			t[4] = E[Foods[i][4]][2];
			t[5] = G[Foods[i][5]][2];
			t[6] = H[Foods[i][6]][2];
			t[7] = K[Foods[i][7]][2];
			for (int j = 1; j < 8; j++)
				if (totalThroughput > t[j])
					totalThroughput = t[j];
			if (totalTime <= maxRT
					&& totalReliability >= minRel
					&& totalThroughput >= minTP && totalCost <= maxCost)
				return true;
			else
				return false;
		}
		else if (serveMethod == 3)
		{
			/*
			 * A[Foods[i][0]][0] B[Foods[i][1]][0] C[Foods[i][2]][0]
			 * D[Foods[i][3]][0] E[Foods[i][4]][0] F[Foods[i][5]][0]
			 * G[Foods[i][6]][0] H[Foods[i][7]][0] I[Foods[i][8]][0]
			 * J[Foods[i][9]][0] K[Foods[i][10]][0] L[Foods[i][11]][0]
			 */
			float time1 = A[Foods[i][0]][0] + B[Foods[i][1]][0]
					+ G[Foods[i][6]][0] + K[Foods[i][10]][0]
					+ L[Foods[i][11]][0];
			float time2 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ D[Foods[i][3]][0] + G[Foods[i][6]][0]
					+ K[Foods[i][10]][0] + L[Foods[i][11]][0];
			float time3 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ E[Foods[i][4]][0] + H[Foods[i][7]][0]
					+ K[Foods[i][10]][0] + L[Foods[i][11]][0];
			float time4 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ F[Foods[i][5]][0] + I[Foods[i][8]][0]
					+ L[Foods[i][11]][0];
			float time5 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ F[Foods[i][5]][0] + J[Foods[i][9]][0]
					+ L[Foods[i][11]][0];
			float[] at = new float[5];
			totalTime = time1;
			at[1] = time2;
			at[2] = time3;
			at[3] = time4;
			at[4] = time5;
			for (int j = 1; j < 5; j++)
				if (totalTime < at[j])
					totalTime = at[j];
			totalReliability = A[Foods[i][0]][1] * B[Foods[i][1]][1]
					* C[Foods[i][2]][1] * D[Foods[i][3]][1] * E[Foods[i][4]][1]
					* F[Foods[i][5]][1] * G[Foods[i][6]][1] * H[Foods[i][7]][1]
					* I[Foods[i][8]][1] * J[Foods[i][9]][1]
					* K[Foods[i][10]][1] * L[Foods[i][11]][0];
			totalCost = A[Foods[i][0]][1] + B[Foods[i][1]][1]
					+ C[Foods[i][2]][1] + D[Foods[i][3]][1] + E[Foods[i][4]][1]
					+ F[Foods[i][5]][1] + G[Foods[i][6]][1] + H[Foods[i][7]][1]
					+ I[Foods[i][8]][1] + J[Foods[i][9]][1]
					+ K[Foods[i][10]][1] + L[Foods[i][11]][0];
			float[] t = new float[12];
			totalThroughput = A[Foods[i][0]][2];
			t[1] = B[Foods[i][1]][2];
			t[2] = C[Foods[i][2]][2];
			t[3] = D[Foods[i][3]][2];
			t[4] = E[Foods[i][4]][2];
			t[5] = F[Foods[i][5]][2];
			t[6] = G[Foods[i][6]][2];
			t[7] = H[Foods[i][7]][2];
			t[8] = I[Foods[i][8]][2];
			t[9] = J[Foods[i][9]][2];
			t[10] = K[Foods[i][10]][2];
			t[11] = L[Foods[i][11]][2];
			for (int j = 1; j < 12; j++)
				if (totalThroughput > t[j])
					totalThroughput = t[j];
			if (totalTime <= maxRT
					&& totalReliability >= minRel
					&& totalThroughput >= minTP && totalCost <= maxCost)
				return true;
			else
				return false;
		}
		else
		{
			/*
			 * A[Foods[i][0]][0] B[Foods[i][1]][0] C[Foods[i][2]][0]
			 * D[Foods[i][3]][0] E[Foods[i][4]][0] F[Foods[i][5]][0]
			 * G[Foods[i][6]][0] H[Foods[i][7]][0] I[Foods[i][8]][0]
			 * J[Foods[i][9]][0] K[Foods[i][10]][0] L[Foods[i][11]][0]
			 * M[Foods[i][12]][0] N[Foods[i][13]][0]
			 */
			float time1 = A[Foods[i][0]][0] + B[Foods[i][1]][0]
					+ G[Foods[i][6]][0] + M[Foods[i][12]][0]
					+ N[Foods[i][13]][0];
			float time2 = A[Foods[i][0]][0] + B[Foods[i][1]][0]
					+ G[Foods[i][6]][0] + K[Foods[i][10]][0]
					+ L[Foods[i][11]][0] + N[Foods[i][13]][0];
			float time3 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ D[Foods[i][3]][0] + +G[Foods[i][6]][0]
					+ M[Foods[i][12]][0] + N[Foods[i][13]][0];
			float time4 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ D[Foods[i][3]][0] + G[Foods[i][6]][0]
					+ K[Foods[i][10]][0] + L[Foods[i][11]][0]
					+ N[Foods[i][13]][0];
			float time5 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ E[Foods[i][4]][0] + H[Foods[i][7]][0]
					+ K[Foods[i][10]][0] + L[Foods[i][11]][0]
					+ N[Foods[i][13]][0];
			float time6 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ F[Foods[i][5]][0] + I[Foods[i][8]][0]
					+ L[Foods[i][11]][0] + N[Foods[i][13]][0];
			float time7 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ F[Foods[i][5]][0] + J[Foods[i][9]][0]
					+ L[Foods[i][11]][0] + N[Foods[i][13]][0];
			float time8 = A[Foods[i][0]][0] + C[Foods[i][2]][0]
					+ F[Foods[i][5]][0] + I[Foods[i][8]][0]
					+ M[Foods[i][12]][0] + N[Foods[i][13]][0];
			float[] at = new float[8];
			totalTime = time1;
			at[1] = time2;
			at[2] = time3;
			at[3] = time4;
			at[4] = time5;
			at[5] = time6;
			at[6] = time7;
			at[7] = time8;
			for (int j = 1; j < 8; j++)
				if (totalTime < at[j])
					totalTime = at[j];
			totalReliability = A[Foods[i][0]][1] * B[Foods[i][1]][1]
					* C[Foods[i][2]][1] * D[Foods[i][3]][1] * E[Foods[i][4]][1]
					* F[Foods[i][5]][1] * G[Foods[i][6]][1] * H[Foods[i][7]][1]
					* I[Foods[i][8]][1] * J[Foods[i][9]][1]
					* K[Foods[i][10]][1] * L[Foods[i][11]][1]
					* M[Foods[i][12]][1] * N[Foods[i][13]][1];
			totalCost = A[Foods[i][0]][1] + B[Foods[i][1]][1]
					+ C[Foods[i][2]][1] + D[Foods[i][3]][1] + E[Foods[i][4]][1]
					+ F[Foods[i][5]][1] + G[Foods[i][6]][1] + H[Foods[i][7]][1]
					+ I[Foods[i][8]][1] + J[Foods[i][9]][1]
					+ K[Foods[i][10]][1] + L[Foods[i][11]][1]
					+ M[Foods[i][12]][1] + N[Foods[i][13]][1];
			float[] t = new float[14];
			totalThroughput = A[Foods[i][0]][2];
			t[1] = B[Foods[i][1]][2];
			t[2] = C[Foods[i][2]][2];
			t[3] = D[Foods[i][3]][2];
			t[4] = E[Foods[i][4]][2];
			t[5] = F[Foods[i][5]][2];
			t[6] = G[Foods[i][6]][2];
			t[7] = H[Foods[i][7]][2];
			t[8] = I[Foods[i][8]][2];
			t[9] = J[Foods[i][9]][2];
			t[10] = K[Foods[i][10]][2];
			t[11] = L[Foods[i][11]][2];
			t[12] = M[Foods[i][12]][2];
			t[13] = N[Foods[i][13]][2];
			for (int j = 1; j < 14; j++)
				if (totalThroughput > t[j])
					totalThroughput = t[j];
			if (totalTime <= maxRT
					&& totalReliability >= minRel
					&& totalThroughput >= minTP && totalCost <= maxCost)
				return true;
			else
				return false;
		}

	}

	/* ��ʼ���Ż��� */
	void initial()
	{
		int i;
		for (i = 0; i < FoodNumber; i++)
		{
			init(i);
		}
		GlobalMin = f[0];
		int flag = 0;
		for (int j = 1; j < 5; j++)
		{
			if (GlobalMin < f[j])
			{
				GlobalMin = f[j];
				flag = j;
			}
		}
		GlobalParams[flag] = solution;

		GlobalMin = f[0];

		for (i = 0; i < d; i++)
		{
			GlobalParams[0][i] = Foods[0][i]; // ��¼���Ž�
			GlobalParams[1][i] = Foods[1][i];
			GlobalParams[2][i] = Foods[2][i];
			GlobalParams[3][i] = Foods[3][i];
			GlobalParams[4][i] = Foods[4][i];
		}
	}

	void SendEmployedBees()
	{
		int i,j,r;
		int rIndex = 0;
		double maxTime = 0;
		double minReliability = 0;
		//��Ӷ��׶�
		for (i = 0; i < FoodNumber; i++)
		{
			for (j = 0; j < d; j++)
				solution[j] = Foods[i][j];
			r = ((int) (Math.random() * d ))+ 1;
			rIndex =(int) (Math.random() * 500 );

			if (serveMethod == 1)
			{
				/*
				 * A[solution[0]][0] B[solution[1]][0] C[solution[2]][0]
				 * D[solution[3]][0] G[solution[4]][0]
				 */
				if (r == 1)
				{
					if (A[rIndex][2] >= minTP)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[rIndex][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[rIndex][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[rIndex][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[solution[4]][1];
					}
				}
				else if (r == 2)
				{
					if (B[rIndex][2] >= minTP)
					{
						if (B[rIndex][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[rIndex][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[rIndex][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[solution[4]][1];
					}
				}
				else if (r == 3)
				{
					if (C[rIndex][2] >= minTP)
					{
						if (B[solution[1]][0] >= C[rIndex][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[rIndex][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[rIndex][1] * D[solution[3]][1]
								* G[solution[4]][1];
					}
				}
				else if (r == 4)
				{
					if (D[rIndex][2] >= minTP)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[rIndex][0])
							maxTime = A[solution[0]][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[rIndex][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[rIndex][1]
								* G[solution[4]][1];
					}
				}
				else if (r == 5)
				{
					if (G[rIndex][2] >= minTP)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[rIndex][0]
									+ G[rIndex][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[rIndex][0];
						minReliability = A[solution[0]][1] * B[rIndex][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[rIndex][1];
					}
				}
			}
			else if (serveMethod == 2)
			{
				if (r == 1)
				{
					/*
					 * A[solution[0]][0] B[solution[1]][0] C[solution[2]][0]
					 * D[solution[3]][0] E[solution[4]][0] G[solution[5]][0]
					 * H[solution[6]][0] K[solution[7]][0]
					 */
					if (A[rIndex][2] >= minTP)
					{
						float time1 = A[rIndex][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[rIndex][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[rIndex][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[rIndex][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 2)
				{
					if (B[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[rIndex][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[rIndex][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 3)
				{
					if (C[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[rIndex][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[rIndex][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[rIndex][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 4)
				{
					if (D[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[rIndex][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[rIndex][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 5)
				{
					if (E[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[rIndex][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[rIndex][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 6)
				{
					if (G[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[rIndex][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[rIndex][1]
								* H[solution[6]][1] * K[solution[7]][1];
					}
				}
				else if (r == 7)
				{
					if (H[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[rIndex][0]
								+ K[solution[7]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[solution[7]][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[solution[7]][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[rIndex][1] * K[solution[7]][1];
					}
				}
				else if (r == 8)
				{
					if (K[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[6]][0]
								+ K[rIndex][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[5]][0]
								+ K[rIndex][0];
						float time3 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[3]][0] + K[rIndex][0];
						if (time1 < time2)
							maxTime = time2;
						else
							maxTime = time1;
						if (maxTime < time3)
							maxTime = time3;
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * G[solution[5]][1]
								* H[solution[6]][1] * K[rIndex][1];
					}
				}
			}
			else if (serveMethod == 3)
			{
				if (r == 1)
				{
					if (A[rIndex][1] >= minTP)
					{
						/*
						 * A[solution[0]][0] B[solution[1]][0] C[solution[2]][0]
						 * D[solution[3]][0] E[solution[4]][0] G[solution[5]][0]
						 * H[solution[6]][0] K[solution[7]][0]
						 */
						float time1 = A[rIndex][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[rIndex][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[rIndex][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[rIndex][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[rIndex][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[rIndex][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 2)
				{
					if (B[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[rIndex][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[rIndex][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 3)
				{
					if (C[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[rIndex][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[rIndex][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[rIndex][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[rIndex][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[rIndex][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 4)
				{
					if (D[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[rIndex][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[rIndex][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 5)
				{
					if (E[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[rIndex][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[rIndex][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 6)
				{
					if (F[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[rIndex][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[rIndex][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[rIndex][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 7)
				{
					if (G[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[rIndex][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[rIndex][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[rIndex][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 8)
				{
					if (H[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[rIndex][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[rIndex][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 9)
				{
					if (I[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[rIndex][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[rIndex][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 10)
				{
					if (J[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[rIndex][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[rIndex][1]
								* K[solution[10]][1] * L[solution[11]][0];
					}
				}
				else if (r == 11)
				{
					if (K[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[rIndex][0]
								+ L[solution[11]][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[rIndex][0] + L[solution[11]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[rIndex][0] + L[solution[11]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[rIndex][1] * L[solution[11]][0];
					}
				}
				else if (r == 12)
				{
					if (L[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[rIndex][0];
						float time2 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[rIndex][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[rIndex][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[rIndex][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[rIndex][0];
						float[] at = new float[5];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						for (j = 1; j < 5; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[rIndex][0];
					}
				}
			}
			else if (serveMethod == 4)
			{
				if (r == 1)
				{
					if (A[rIndex][1] >= minTP)
					{
						/*
						 * A[rIndex][0] B[solution[1]][0] C[solution[2]][0]
						 * D[solution[3]][0] E[solution[4]][0] G[solution[5]][0]
						 * H[solution[6]][0] K[solution[7]][0]
						 */
						float time1 = A[rIndex][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ M[solution[12]][0];
						float time2 = A[rIndex][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[rIndex][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[rIndex][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[rIndex][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[rIndex][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[rIndex][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[rIndex][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[rIndex][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 2)
				{
					if (B[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[rIndex][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[rIndex][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[rIndex][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 3)
				{
					if (C[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[rIndex][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[rIndex][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[rIndex][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[rIndex][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[rIndex][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[rIndex][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[rIndex][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 4)
				{
					if (D[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[rIndex][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[rIndex][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[rIndex][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 5)
				{
					if (E[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[rIndex][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[rIndex][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 6)
				{
					if (F[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[rIndex][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[rIndex][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[rIndex][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[rIndex][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 7)
				{
					if (G[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[rIndex][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[rIndex][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[rIndex][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[rIndex][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[rIndex][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 8)
				{
					if (H[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[rIndex][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[rIndex][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 9)
				{
					if (I[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[rIndex][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[rIndex][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[rIndex][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 10)
				{
					if (J[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[rIndex][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[rIndex][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 11)
				{
					if (K[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[rIndex][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[rIndex][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[rIndex][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[rIndex][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 12)
				{
					if (L[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[rIndex][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[rIndex][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[rIndex][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[rIndex][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[rIndex][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[rIndex][1]
								* M[solution[12]][1] * N[solution[13]][1];
					}
				}
				else if (r == 13)
				{
					if (M[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[rIndex][0]
								+ N[solution[13]][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[rIndex][0] + N[solution[13]][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[solution[13]][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[solution[13]][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[rIndex][0] + N[solution[13]][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[rIndex][1] * N[solution[13]][1];
					}
				}
				else if (r == 14)
				{
					if (N[rIndex][2] >= minTP)
					{
						float time1 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + M[solution[12]][0]
								+ N[rIndex][0];
						float time2 = A[solution[0]][0] + B[solution[1]][0]
								+ G[solution[6]][0] + K[solution[10]][0]
								+ L[solution[11]][0] + N[rIndex][0];
						float time3 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + +G[solution[6]][0]
								+ M[solution[12]][0] + N[rIndex][0];
						float time4 = A[solution[0]][0] + C[solution[2]][0]
								+ D[solution[3]][0] + G[solution[6]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[rIndex][0];
						float time5 = A[solution[0]][0] + C[solution[2]][0]
								+ E[solution[4]][0] + H[solution[7]][0]
								+ K[solution[10]][0] + L[solution[11]][0]
								+ N[rIndex][0];
						float time6 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ L[solution[11]][0] + N[rIndex][0];
						float time7 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + J[solution[9]][0]
								+ L[solution[11]][0] + N[rIndex][0];
						float time8 = A[solution[0]][0] + C[solution[2]][0]
								+ F[solution[5]][0] + I[solution[8]][0]
								+ M[solution[12]][0] + N[rIndex][0];
						float[] at = new float[8];
						maxTime = time1;
						at[1] = time2;
						at[2] = time3;
						at[3] = time4;
						at[4] = time5;
						at[5] = time6;
						at[6] = time7;
						at[7] = time8;
						for (j = 1; j < 8; j++)
							if (maxTime < at[j])
								maxTime = at[j];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* E[solution[4]][1] * F[solution[5]][1]
								* G[solution[6]][1] * H[solution[7]][1]
								* I[solution[8]][1] * J[solution[9]][1]
								* K[solution[10]][1] * L[solution[11]][1]
								* M[solution[12]][1] * N[rIndex][1];
					}
				}
				if (maxTime <= maxRT
						&& minReliability >= minRel)
					solution[r - 1] = rIndex;
				ObjValSol = calculateFunction(solution);
				FitnessSol = CalculateFitness(ObjValSol);
				if (ObjValSol > f[i])
				{
					trial[i] = 0;
					for (j = 0; j < d; j++)
						Foods[i][j] = solution[j];
					f[i] = ObjValSol;
					fitness[i] = FitnessSol;
				}
				else
				{
					trial[i] = trial[i] + 1;
				}

				double[] t = new double[5];
				int flag = 0;
				GlobalMin = calculateFunction(GlobalParams[0]);
				t[1] = calculateFunction(GlobalParams[1]);
				t[2] = calculateFunction(GlobalParams[2]);
				t[3] = calculateFunction(GlobalParams[3]);
				t[4] = calculateFunction(GlobalParams[4]);
				for (int k = 1; k < 5; k++)
					if (GlobalMin < t[k])
					{
						GlobalMin = t[k];
						flag = k;
					}
				GlobalParams[flag] = solution;
			}
		}
	}

	/* ʳ��Դ��ѡ�еĸ��ʼ���Ϊ�� prob(i)=fitness(i)/sum(fitness) */
	void CalculateProbabilities()
	{
		double sumfit = 0;
		for (int i = 0; i < FoodNumber; i++)
		{
			sumfit += fitness[i];
		}
		for (int i = 0; i < FoodNumber; i++)
		{
			prob[i] = fitness[i] / sumfit;
		}
	}

	/* �۲��׶� */
	void SendOnlookerBees()
	{
		int i, j, t;
		int randomValue1;
		int randomSelect = 0;
		double maxTime = 0, minReliability = 0;
		i = 0;
		t = 0;
		/* onlooker Bee Phase */
		while (t < FoodNumber)
		{
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1))); // �������һ������
			if (r < prob[i])
			{
				t++;

				for (j = 0; j < d; j++)
					solution[j] = Foods[i][j];

				randomValue1 = (int) Math.round(Math.random() * (d - 1) + 1);
				randomSelect = (int) Math.round(Math.random() * (500 - 1) + 1) - 1;

				if (randomValue1 == 1)
				{
					if (A[randomSelect][2] >= minTP)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[randomSelect][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[randomSelect][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[randomSelect][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[solution[4]][1];
						if (maxTime <= maxRT
								&& minReliability >= minRel)
							solution[randomValue1 - 1] = randomSelect;
					}
				}
				else if (randomValue1 == 2)
				{
					if (B[randomSelect][2] >= 30)
					{
						if (B[randomSelect][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[randomSelect][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[randomSelect][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[solution[4]][1];
						if (maxTime <= maxRT
								&& minReliability >= minRel)
							solution[randomValue1 - 1] = randomSelect;
					}
				}
				else if (randomValue1 == 3)
				{
					if (C[randomSelect][2] >= 30)
					{
						if (B[solution[1]][0] >= C[randomSelect][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[randomSelect][0]
									+ D[solution[3]][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[randomSelect][1] * D[solution[3]][1]
								* G[solution[4]][1];
						if (maxTime <= maxRT
								&& minReliability >= minRel)
							solution[randomValue1 - 1] = randomSelect;
					}
				}
				else if (randomValue1 == 4)
				{
					if (D[randomSelect][2] >= 30)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[randomSelect][0])
							maxTime = A[solution[0]][0] + B[solution[1]][0]
									+ G[solution[4]][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[randomSelect][0] + G[solution[4]][0];
						minReliability = A[solution[0]][1] * B[solution[1]][1]
								* C[solution[2]][1] * D[randomSelect][1]
								* G[solution[4]][1];
						if (maxTime <= maxRT
								&& minReliability >= minRel)
							solution[randomValue1 - 1] = randomSelect;
					}
				}
				else if (randomValue1 == 5)
				{
					if (G[randomSelect][2] >= 30)
					{
						if (B[solution[1]][0] >= C[solution[2]][0]
								+ D[solution[3]][0])
							maxTime = A[solution[0]][0] + B[randomSelect][0]
									+ G[randomSelect][0];
						else
							maxTime = A[solution[0]][0] + C[solution[2]][0]
									+ D[solution[3]][0] + G[randomSelect][0];
						minReliability = A[solution[0]][1] * B[randomSelect][1]
								* C[solution[2]][1] * D[solution[3]][1]
								* G[randomSelect][1];
						if (maxTime <= maxRT
								&& minReliability >= minRel)
							solution[randomValue1 - 1] = randomSelect;
					}
				}

				ObjValSol = calculateFunction(solution);
				FitnessSol = CalculateFitness(ObjValSol);
				if (ObjValSol > f[i])
				{
					trial[i] = 0;
					for (j = 0; j < d; j++)
						Foods[i][j] = solution[j];
					f[i] = ObjValSol;
					fitness[i] = FitnessSol;
				}
				else
				{
					trial[i] = trial[i] + 1;
				}

				GlobalMin = calculateFunction(GlobalParams[0]);
				int flag = 0;
				for (int k = 1; k < 5; k++)
				{
					if (GlobalMin < calculateFunction(GlobalParams[k]))
					{
						GlobalMin = calculateFunction(GlobalParams[k]);
						flag = k;
					}
				}
				GlobalParams[flag] = solution;

				if (f[i] < GlobalMin) // Ҫ��Ŀ�꺯��ֵԽСԽ�� ���������ԽСԽ��
				{
					for (j = 0; j < d; j++)
						GlobalParams[flag][j] = solution[j];
					GlobalMin = f[i];
				}

			}
			i++;
			if (i == FoodNumber)
				i = 0;
		}
	}

	/* ����׶� */
	void SendScoutBees()
	{
		int maxtrialindex, i;
		maxtrialindex = 0;
		for (i = 1; i < FoodNumber; i++)
		{
			if (trial[i] > trial[maxtrialindex])
				maxtrialindex = i;
		}
		if (trial[maxtrialindex] >= limit)
		{
			init(maxtrialindex); // ÿ��ֻ��������һ����Դ ����һ������Դ
		}
	}

	/* ����Ŀ�꺯����ֵ */
	double calculateFunction(int sol[])
	{
		float sum = 0;
		switch (serveMethod)
		{
		case 1:
			sum = A[sol[0]][3] + B[sol[1]][3] + C[sol[2]][3] + D[sol[3]][3]
					+ G[sol[4]][3];
			break;
		case 2:
			sum = A[sol[0]][3] + B[sol[1]][3] + C[sol[2]][3] + D[sol[3]][3]
					+ E[sol[4]][3] + G[sol[5]][3] + H[sol[6]][3] + K[sol[7]][3];
			break;
		case 3:
			sum = A[sol[0]][3] + B[sol[1]][3] + C[sol[2]][3] + D[sol[3]][3]
					+ E[sol[4]][3] + F[sol[5]][3] + G[sol[6]][3] + H[sol[7]][3]
					+ I[sol[8]][3] + J[sol[9]][3] + K[sol[10]][3]
					+ L[sol[11]][3];
			break;
		case 4:
			sum = A[sol[0]][3] + B[sol[1]][3] + C[sol[2]][3] + D[sol[3]][3]
					+ E[sol[4]][3] + F[sol[5]][3] + G[sol[6]][3] + H[sol[7]][3]
					+ I[sol[8]][3] + J[sol[9]][3] + K[sol[10]][3]
					+ L[sol[11]][3] + M[sol[12]][3] + N[sol[13]][3];
			break;
		default:
			txtLogs.append("\n" + "������");
		}

		return sum;
	}
}
